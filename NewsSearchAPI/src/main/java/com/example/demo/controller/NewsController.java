package com.example.demo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.News;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;


@RequestMapping("/API-IMPLEMENTADO")
@RestController
public class NewsController {
    /**
     * @param query
     * @return
     */
    @GetMapping("/consulta") 
    public ResponseEntity<List<News>> searchNews(@RequestParam("q") String query) throws IOException {
    	if (query == null || query.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(null);
        }
    	try {
	        List<News> newsList = new ArrayList<>();
			String searchUrl = "https://www.abc.com.py/buscador/?query="+query;
			Playwright playwright = Playwright.create();
			final BrowserType chromium = playwright.chromium();
			final Browser browser = chromium.launch();
			Page page = browser.newPage();
			page.navigate(searchUrl);
			page.waitForLoadState(LoadState.NETWORKIDLE);
			final ElementHandle contentElement = page.querySelector("[id=resultdata]");
			System.out.println("contentElement:"+contentElement);
			String contentHTML = contentElement.innerHTML();
		    Document doc = Jsoup.parse(contentHTML);
		    Elements newsElements = getItems(doc);
			System.out.println("Total:" + newsElements.size());
			for (Element newsElement : newsElements) {
				String contenido = newsElement.selectFirst("div[style*=margin-bottom]").text();
				String fecha = contenido.substring(0, 16); // Extrae los primeros 16 caracteres que corresponden a la fecha
				System.out.println("fecha:"+fecha);
				
				String enlace = newsElement.selectFirst("a")
	                     .attr("href");
				System.out.println("enlace:"+enlace);
				
				String enlaceFotoStyle = newsElement.selectFirst("div.queryly_advanced_item_imagecontainer")
	                    .attr("style");
				String enlaceFoto = enlaceFotoStyle.substring(enlaceFotoStyle.indexOf("url('") + 5, enlaceFotoStyle.indexOf("')"));
				System.out.println("enlaceFoto:"+enlaceFoto);
				
				String titulo = newsElement.selectFirst("div.queryly_item_title")
	                    .text();
				System.out.println("titulo:"+titulo);
				
				String resumen = newsElement.selectFirst("div.queryly_item_description")
	                     .text();
				System.out.println("resumen:"+resumen);
			    
			    News news = new News(fecha, enlace, enlaceFoto, titulo, resumen);
			    newsList.add(news);
	
			}
			if (newsList.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                                .body(null);
	        }
			
			return ResponseEntity.status(HttpStatus.OK)
			                    .body(newsList);
			
    	}catch (Exception e) {
		    e.printStackTrace();
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body(null);
		}
    }
    public static Elements getItems(Document document) {
		return document.select("div.queryly_item_row");
	}
    
}

