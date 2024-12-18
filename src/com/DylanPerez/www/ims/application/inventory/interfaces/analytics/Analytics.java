package com.DylanPerez.www.ims.application.inventory.interfaces.analytics;

public interface Analytics<K, V> {

    boolean addData(K key);

    boolean removeData(K key);

    V getData(K key);

    void generate();

}
