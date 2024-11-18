package com.DylanPerez.www.ims.service.analytics;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SaleAnalytics implements Analytics<String, Integer> {

    private final Map<Integer, Set<String>> salesCounts; // Map<# of sales, set of SKUs>

    public SaleAnalytics(Set<String> productSkus) {
        salesCounts = new TreeMap<>();
        productSkus.forEach(sku -> salesCounts.merge(0, new HashSet<>(), (k, v) -> {
            v.add(sku);
            return v;
        }));
    }

    @Override
    public boolean addData(String key) {
        Integer saleCount = getData(key);
        if(saleCount == null) return false;

        salesCounts.get(saleCount).remove(key);
        salesCounts.compute(saleCount + 1, (k, v) -> {
            if(v == null)
                v = new HashSet<>();
            v.add(key);
            return v;
        });

        return true;
    }

    @Override
    public boolean removeData(String key) {

        Integer saleCount = getData(key);
        if(saleCount == null || saleCount - 1 < 0) return false;

        salesCounts.get(saleCount).remove(key);
        salesCounts.compute(saleCount - 1, (k, v) -> {
            if(v == null) // Shouldn't ever be true
                v = new HashSet<>();
            v.add(key);
            return v;
        });

        return true;
    }

    @Override
    public Integer getData(String key) {
        for(Integer saleCount : salesCounts.keySet())
            if(salesCounts.get(saleCount).contains(key)) return saleCount;
        return null;
    }

    @Override
    public void generateAnalytics() {



    }
}
