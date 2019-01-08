package tech.wetech.weshop.service.impl;

import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.wetech.weshop.mapper.KeywordsMapper;
import tech.wetech.weshop.mapper.SearchHistoryMapper;
import tech.wetech.weshop.po.Keywords;
import tech.wetech.weshop.po.SearchHistory;
import tech.wetech.weshop.service.SearchService;
import tech.wetech.weshop.utils.Constants;
import tech.wetech.weshop.utils.Reflections;
import tech.wetech.weshop.vo.SearchIndexVO;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private KeywordsMapper keywordsMapper;

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    @Override
    public List<String> helper(String keyword) {
        Weekend<Keywords> example = Weekend.of(Keywords.class);
        example.selectProperties(Reflections.fnToFieldName(Keywords::getKeyword));
        WeekendCriteria<Keywords, Object> criteria = example.weekendCriteria();
        criteria.andLike(Keywords::getKeyword, "%" + keyword + "%");

        return PageHelper.startPage(1, 10).doSelectPage(() ->
                keywordsMapper.selectByExample(example).stream()
                        .map(Keywords::getKeyword)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public void clearHistory() {
        searchHistoryMapper.delete(new SearchHistory().setUserId(Constants.CURRENT_USER_ID));
    }

    @Override
    public SearchIndexVO index() {
        // 取出输入框默认的关键词
        PageHelper.offsetPage(0, 1);
        Keywords defaultKeyword = keywordsMapper.selectOne(new Keywords().setrequireDefault(true));
        // 取出热闹关键词
        PageHelper.startPage(1, 10);
        List<Keywords> hotKeywordList = keywordsMapper.select(new Keywords().setHot(true));

        PageHelper.startPage(1, 10);
        List<String> historyKeywordList = searchHistoryMapper.select(new SearchHistory().setUserId(Constants.CURRENT_USER_ID)).stream()
                .map(SearchHistory::getKeyword)
                .collect(Collectors.toList());

        return new SearchIndexVO()
                .setDefaultKeyword(defaultKeyword)
                .setHotKeywordList(hotKeywordList)
                .setHistoryKeywordList(historyKeywordList);
    }

}
