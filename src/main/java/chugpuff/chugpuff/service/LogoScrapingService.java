package chugpuff.chugpuff.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service
public class LogoScrapingService {

    public List<String> getCompanyLogos(String url) {
        List<String> logoUrls = new ArrayList<>();

        try {
            // Jsoup을 사용하여 HTML 문서를 파싱
            Document document = Jsoup.connect(url).get();

            // 예를 들어, img 태그의 특정 클래스명을 기준으로 로고 이미지 URL을 추출
            Elements imgElements = document.select("img.logo");

            for (Element imgElement : imgElements) {
                String logoUrl = imgElement.attr("src");
                logoUrls.add(logoUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logoUrls;
    }
}