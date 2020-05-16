package requests.client;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.img;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.Elements.table;
import static org.jboss.gwt.elemento.core.Elements.tbody;
import static org.jboss.gwt.elemento.core.Elements.td;
import static org.jboss.gwt.elemento.core.Elements.th;
import static org.jboss.gwt.elemento.core.Elements.thead;
import static org.jboss.gwt.elemento.core.Elements.tr;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.safehtml.shared.UriUtils;
import com.intendia.gwt.autorest.client.AutoRestGwt;
import com.intendia.gwt.autorest.client.RequestResourceBuilder;
import com.intendia.rxgwt2.elemento.RxElemento;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import requests.client.Requests.Nominatim.SearchResult;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventType;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.gwt.elemento.core.builder.HtmlContent;

public class Requests implements EntryPoint {

    @Override
    public void onModuleLoad() {
        Nominatim service = new Nominatim_RestServiceModel(() -> new RequestResourceBuilder()
                .path("https://nominatim.openstreetmap.org/"));
        HTMLInputElement search = Elements.input(InputType.search).get();
        HTMLDivElement out = Elements.div().add(div()).get();
        Elements.body().add(Elements.div().add("query: ").add(search)).add(out);

        Observable<String> value$ = Observable.merge(
                RxElemento.fromEvent(search, EventType.change),
                RxElemento.fromEvent(search, EventType.keyup))
                .map(ev -> Js.<HTMLInputElement>cast(ev.target).value);

        // table html builders
        HTMLElement thead = thead().add(tr()
                .add(th().add("Symbol"))
                .add(th().add("Coordinates"))
                .add(th().add("Postcode"))
                .add(th().add("Summary"))).get();
        Function<SearchResult, HTMLElement> tr = o -> tr()
                .add(td().add(img(UriUtils.encode(o.icon == null ? "" : o.icon))))
                .add(td().add(o.lat).add(",").add(o.lon))
                .add(td().add(o.getPostcode()))
                .add(td().add(o.display_name)).get();

        value$.throttleLatest(300, TimeUnit.MILLISECONDS, true)
                .switchMapSingle(ev -> service.search(search.value, "json", 1)
                        .map(tr).collectInto(tbody(), HtmlContent::add)
                        .<HTMLElement>map(tbody -> table().add(thead).add(tbody).get())
                        .onErrorReturn(ex -> span().style("color: darkred").add("error: " + ex).get()))
                .subscribe(n -> out.replaceChild(n, out.firstChild));
    }

    @AutoRestGwt
    @javax.ws.rs.Path("search")
    public interface Nominatim {

        @GET
        Observable<SearchResult> search(
                @QueryParam("q") String query,
                @QueryParam("format") String format,
                @QueryParam("addressdetails") Integer addressDetails);

        @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
        class SearchResult {
            public String display_name, icon, lon, lat;
            public Address address;

            @JsOverlay
            public final String getPostcode() {
                return address == null ? "" : address.postcode;
            }
        }

        @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
        class Address {
            public String city, country, postcode;
        }
    }
}
