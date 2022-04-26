package com.gmail.necnionch.myplugin.bungeeplaytime.common.dev;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.errors.RemoteHandleError;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.errors.RemoteNotImplemented;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.errors.SenderError;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.RequestHandler;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.ResponseHandler;
import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class DataIO {
    private final Map<String, RequestHandler<? extends Request<? extends Response>, ? extends Response>> requestHandlers = Maps.newHashMap();
    private final Map<String, ResponseHandler<? extends Response>> responseHandlers = Maps.newHashMap();
    private final Executor syncExecutor;
    private final Executor asyncExecutor;
    private final AtomicInteger requestLastId = new AtomicInteger();
    private final Map<Integer, CompletableFuture<? extends Response>> waitTasks = Maps.newConcurrentMap();
    private final Logger logger;


    public DataIO(Logger logger, Executor syncExecutor, Executor asyncExecutor) {
        this.logger = logger;
        this.syncExecutor = syncExecutor;
        this.asyncExecutor = asyncExecutor;
    }


    public <R extends Request<Res>, Res extends Response> void registerHandler(String targetDataKey, RequestHandler<R, Res> handler) {
        if (requestHandlers.containsKey(targetDataKey))
            throw new IllegalArgumentException("already registered data-key: " + targetDataKey);
        requestHandlers.put(targetDataKey, handler);
    }

    public <R extends Response> void registerHandler(String targetDataKey, ResponseHandler<R> handler) {
        if (responseHandlers.containsKey(targetDataKey))
            throw new IllegalArgumentException("already registered data-key: " + targetDataKey);
        responseHandlers.put(targetDataKey, handler);
    }


    public <R extends Request<Res>, Res extends Response> Requested<Res> send(R request) {
        int reqId = requestLastId.incrementAndGet();
        CompletableFuture<Res> future = new CompletableFuture<>();

        // queue
        waitTasks.put(reqId, future);

        // send
        asyncExecutor.execute(() -> {
            //noinspection UnstableApiUsage
            ByteArrayDataOutput output = ByteStreams.newDataOutput();
            output.writeInt(SendType.REQUEST.getValue());
            output.writeInt(reqId);
            output.writeUTF(request.getDataKey());
            request.serialize(output);
            writeOut(output.toByteArray());
        });

        return new Requested<>(future, syncExecutor);
    }


    protected abstract void writeOut(byte[] data);

    protected void writeIn(byte[] data) {
        try {
            //noinspection UnstableApiUsage
            ByteArrayDataInput input = ByteStreams.newDataInput(data);
            SendType sendType = SendType.valueOf(input.readInt());
            int reqId = input.readInt();
            String dataKey = input.readUTF();

            switch (sendType) {
                case RESPONSE: {
                    onResponse(reqId, dataKey, input);
                    break;
                }

                case REQUEST: {
                    onRequest(reqId, dataKey, input);
                    break;
                }

                case RESPONSE_ERROR: {
                    onResponseError(reqId, dataKey, input);
                    break;
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    private <Res extends Response> void onResponse(int reqId, String dataKey, ByteArrayDataInput input) {
        //noinspection unchecked
        CompletableFuture<Res> future = (CompletableFuture<Res>) waitTasks.remove(reqId);
        if (future == null)
            return;

        try {
            //noinspection unchecked
            ResponseHandler<Res> handler = (ResponseHandler<Res>) responseHandlers.get(dataKey);
            if (handler == null) {
                logger.warning("Unknown response (dataKey: " + dataKey + ")");
                return;
            }

            try {
                Res response = handler.handleResponse(input);
                future.complete(response);

            } catch (Throwable e) {
                future.completeExceptionally(e);
                logger.log(Level.WARNING, "Exception in response handling (dataKey: " + dataKey + ")", e);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private <Res extends Response> void onResponseError(int reqId, String dataKey, ByteArrayDataInput input) {
        //noinspection unchecked
        CompletableFuture<Res> future = (CompletableFuture<Res>) waitTasks.remove(reqId);
        if (future == null)
            return;

        String message = input.readUTF();
        Throwable error;
        switch (message) {
            case "notImplementedError":
                error = new RemoteNotImplemented("not implemented -> " + dataKey);
                logger.warning("Unknown request by remote (dataKey: " + dataKey + ")");
                break;
            case "handleError":
                error = new RemoteHandleError("internal error");
                logger.warning("Exception in request handling by remote (dataKey: " + dataKey + ")");
                break;
            default:
                error = new SenderError("unknown error");
                logger.warning("Unknown exception in request handling by remote (dataKey: " + dataKey + ")");
        }
        future.completeExceptionally(error);
    }

    private <R extends Request<Res>, Res extends Response> void onRequest(int reqId, String dataKey, ByteArrayDataInput input) {
        //noinspection unchecked
        RequestHandler<R, Res> handler = (RequestHandler<R, Res>) requestHandlers.get(dataKey);
        if (handler == null) {
            ByteArrayDataOutput output = createPacket(SendType.RESPONSE_ERROR, reqId, dataKey);
            output.writeUTF("notImplementedError");
            writeOut(output.toByteArray());
            logger.warning("Unknown request (dataKey: " + dataKey + ")");
            return;
        }

        try {
            R request = handler.handleRequest(input);
            Res response = handler.processRequest(request);

            ByteArrayDataOutput output = createPacket(SendType.RESPONSE, reqId, dataKey);
            response.serialize(output);
            writeOut(output.toByteArray());

        } catch (Throwable e) {
            ByteArrayDataOutput output = createPacket(SendType.RESPONSE_ERROR, reqId, dataKey);
            output.writeUTF("handleError");
            writeOut(output.toByteArray());
            logger.log(Level.WARNING, "Exception in request handling (dataKey: " + dataKey + ")", e);
        }
    }


    private ByteArrayDataOutput createPacket(SendType sendType, int reqId, String dataKey) {
        //noinspection UnstableApiUsage
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeInt(sendType.getValue());
        output.writeInt(reqId);
        output.writeUTF(dataKey);
        return output;
    }



    public static final class Requested<Res> {
        private final CompletableFuture<Res> future;
        private final Executor executor;

        public Requested(CompletableFuture<Res> future, Executor executor) {
            this.future = future;
            this.executor = executor;
        }

        public void whenComplete(BiConsumer<Res, Throwable> action) {
            future.whenComplete((ret, err) -> {
                try {
                    executor.execute(() -> action.accept(ret, err));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }

    }

    public enum SendType {
        REQUEST(0), RESPONSE(1), RESPONSE_ERROR(2);

        private final int value;
        SendType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SendType valueOf(int value) {
            switch (value) {
                case 0:
                    return REQUEST;
                case 1:
                    return RESPONSE;
                case 2:
                    return RESPONSE_ERROR;
                default:
                    throw new IllegalArgumentException("unknown send-type: " + value);
            }
        }

    }

}