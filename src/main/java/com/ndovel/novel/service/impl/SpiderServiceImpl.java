package com.ndovel.novel.service.impl;

import com.ndovel.novel.config.SpiderProperties;
import com.ndovel.novel.exception.DataIsNotExistException;
import com.ndovel.novel.exception.RequestException;
import com.ndovel.novel.model.dto.*;
import com.ndovel.novel.model.entity.*;
import com.ndovel.novel.repository.*;
import com.ndovel.novel.service.AsyncService;
import com.ndovel.novel.service.SpiderService;
import com.ndovel.novel.spider.core.IndexSpider;
import com.ndovel.novel.spider.core.NovelSpider;
import com.ndovel.novel.spider.core.SearchSpider;
import com.ndovel.novel.spider.core.SpiderFactory;
import com.ndovel.novel.spider.core.impl.*;
import com.ndovel.novel.spider.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Slf4j
@Service
public class SpiderServiceImpl implements SpiderService {
    private SpiderProperties spiderProperties;
    private AuthorRepository authorRepository;
    private MatchRexRepository matchRexRepository;
    private BookRepository bookRepository;
    private VisitRepository visitRepository;
    private SpiderInfoRepository spiderInfoRepository;
    private AsyncService asyncService;
    private SpiderFactory spiderFactory;

    public SpiderServiceImpl(AuthorRepository authorRepository,
                             MatchRexRepository matchRexRepository,
                             BookRepository bookRepository,
                             VisitRepository visitRepository,
                             SpiderInfoRepository spiderInfoRepository,
                             AsyncService asyncService,
                             SpiderProperties spiderProperties,
                             SpiderFactory spiderFactory) {
        this.authorRepository = authorRepository;
        this.matchRexRepository = matchRexRepository;
        this.bookRepository = bookRepository;
        this.visitRepository = visitRepository;
        this.spiderInfoRepository = spiderInfoRepository;
        this.asyncService = asyncService;
        this.spiderProperties = spiderProperties;
        this.spiderFactory = spiderFactory;
    }

    @Override
    public BookDTO spider(SpiderIndex spiderIndex) {
        SpiderInfo spiderInfo = new SpiderInfo();
        spiderInfo.setUrl(spiderIndex.getFirstChapterUrl());

        spiderInfo.setMatchRex(matchRexRepository.findOneIsExist(spiderIndex.getMatchRexId())
                .orElseGet(()->{
                    Page<MatchRex> isExist = matchRexRepository.findIsExist(PageRequest.of(0, 1));
                    if (isExist.getTotalElements() > 0){
                        return  isExist.getContent().get(0);
                    } else {
                        throw new DataIsNotExistException();
                    }
                }));

        log.info("???????????????" + spiderIndex.getBookName());

        Book book = new Book();
        book.setName(spiderIndex.getBookName());

        Author author = authorRepository.findOneByName(spiderIndex.getAuthorName());
        if(author == null) {
            author = new Author();
            author.setName(spiderIndex.getAuthorName());
            authorRepository.save(author);
        }
        book.setAuthor(author);
        book.setIntroduce(spiderIndex.getIntroduce());

        bookRepository.save(book);
        try {
            saveImg(spiderIndex.getCoverUrl(), String.valueOf(book.getId()));
        } catch (RequestException | IOException e) {
            log.error("??????????????????");
        }

        //????????????
        Visit visit = new Visit();
        visit.setBookId(book.getId());
        visit.setVisit(0L);
        visitRepository.save(visit);

        spiderInfo.setBook(book);

        asyncService.down(spiderInfo);

        return new BookDTO().init(book);
    }

    @Override
    public TempChapter spiderOne(String url, Integer matchRexId) {
        log.info("???????????????" + url);
        NovelSpider spider = getSpider(url, matchRexId);
        spider.run();
        return spider.getTempChapter();
    }

    @Override
    public SpiderInfoDTO update(Integer spiderInfoId) {
        log.info("???????????????" + spiderInfoId);
        Object obj = spiderInfoRepository.findOneIsExist(spiderInfoId).map(spiderInfo -> {
            asyncService.down(spiderInfo, true);
            return new SpiderInfoDTO().init(spiderInfo);
        }).orElse(null);
        return (SpiderInfoDTO)obj;
    }

    @Override
    public List<SearchResult> spiderByName(String name) {
        log.info("???????????????" + name);
        SearchSpider searchSpider = spiderFactory.newSearchSpider();
        return searchSpider.search(name);
    }

    @Override
    public TempBook spiderByIndex(String url) {
        IndexSpider indexSpider = spiderFactory.newIndexSpider();
        return indexSpider.getTempBook(url);
    }

    private NovelSpider getSpider(String url, Integer matchRexId){
        SpiderInfoDTO spiderInfo = new SpiderInfoDTO();
        spiderInfo.setUrl(url);
        MatchRex rex = null;
        if (matchRexId > 0){
            rex = matchRexRepository.findOneIsExist(matchRexId).orElse(null);
        }
        if (rex == null) {
            Pageable pageable = PageRequest.of(0,1);
            Page<MatchRex> page = matchRexRepository.findIsExist(pageable);
            if (page.getTotalElements() > 0){
                rex =  page.getContent().get(0);
            }else {
                throw new DataIsNotExistException();
            }
            spiderInfo.setMatchRex(new MatchRexDTO().init(rex));
        }

        return spiderFactory.newNovelSpider(spiderInfo);
    }

    @Override
    public void saveImg(String imgUrl, String imgName) throws RequestException, IOException {
        File file = new File(spiderProperties.getCoverPath());
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }
        byte[] byteArray = HttpClientUtils.getByteArray(imgUrl);

        try(OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(file, imgName + ".jpg")))){
            os.write(byteArray);
            os.flush();
        }
    }

}
