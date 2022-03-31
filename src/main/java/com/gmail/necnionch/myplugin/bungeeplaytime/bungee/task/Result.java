package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.task;

public class Result<T> {
    private T object;
    private Throwable throwable;


    public void setResult(T obj) {
        if (object != null || throwable != null)
            throw new IllegalStateException("already set result");

        object = obj;
    }

    public void setException(Throwable throwable) {
        if (object != null || this.throwable != null)
            throw new IllegalStateException("already set result");

        this.throwable = throwable;
    }

    public T result() throws Throwable {
        if (throwable == null)
            return object;
        throw throwable;
    }

}
