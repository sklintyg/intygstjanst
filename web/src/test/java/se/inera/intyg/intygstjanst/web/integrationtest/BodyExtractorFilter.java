package se.inera.intyg.intygstjanst.web.integrationtest;

import java.util.Map;

import com.jayway.restassured.builder.ResponseBuilder;
import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;

public class BodyExtractorFilter implements Filter {

    private final String extractXPath;
    private final Map<String, String> namespaceMap;

    public BodyExtractorFilter(Map<String, String> namespaceMap, String extractXPath) {
        this.namespaceMap = namespaceMap;
        this.extractXPath = extractXPath;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec);
        XPathExtractor extractor = new XPathExtractor(response.print(), namespaceMap);
        String newBody = extractor.getFragmentFromXPath(extractXPath);
        return new ResponseBuilder().clone(response).setBody(newBody).build();
    }

}
