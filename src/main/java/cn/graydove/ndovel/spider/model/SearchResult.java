package cn.graydove.ndovel.spider.model;

import lombok.Data;

import java.util.List;

/**
 * @author graydove
 */
@Data
public class SearchResult {

    List<TextLink> result;

    String nextPageUrl;
}
