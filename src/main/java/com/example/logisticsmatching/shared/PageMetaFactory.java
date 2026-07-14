package com.example.logisticsmatching.shared;

import com.example.logisticsmatching.generated.openapi.PageMeta;

/**
 * ページング応答共通化（IMPL-09）。
 *
 * <p>一覧系 operationId（{@code listJobs}, {@code listMyBids}, {@code listNotifications} 等）が
 * 共通で使う {@code PageMeta}（{@code api/_common.yaml} 正典）の算出ロジック。
 */
public final class PageMetaFactory {

    private PageMetaFactory() {}

    public static PageMeta of(int page, int size, long totalElements) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        PageMeta meta = new PageMeta();
        meta.setPage(page);
        meta.setSize(size);
        meta.setTotalElements(Math.toIntExact(totalElements));
        meta.setTotalPages(totalPages);
        return meta;
    }
}
