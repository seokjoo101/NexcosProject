package com.example.seokjoo.contactex.global;

/**
 * Created by Seokjoo on 2016-08-09.
 */
public class BasePresenter  <T extends BasePresenterView> {

    private T view;

    public BasePresenter(T view) {
        this.view = view;
    }

    protected T getView() {
        return view;
    }
}
