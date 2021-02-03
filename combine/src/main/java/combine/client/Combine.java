package combine.client;

import static com.intendia.rxgwt2.elemento.RxElemento.fromEvent;
import static elemental2.dom.DomGlobal.document;
import static io.reactivex.Observable.just;
import static io.reactivex.Observable.merge;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.EventType.mousemove;
import static org.jboss.gwt.elemento.core.EventType.touchmove;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLLabelElement;
import io.reactivex.Observable;
import jsinterop.base.Js;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;

public class Combine implements EntryPoint {

    @Override
    public void onModuleLoad() {
        HTMLElement out = Elements.span().style("font-weight: bold;").get();
        HTMLInputElement input = input(InputType.checkbox).checked(true).get();
        HtmlContentBuilder<HTMLLabelElement> label = label("Activate coordinates").add(input);
        Elements.body().add(div().add("pointer position: ").add(out)).add(label);

        // preparation of the 3 data sources including mapping and initialization…
        Observable<int[]> mouseCoordinates$ = fromEvent(document, mousemove)
                .map(ev -> new int[] { (int) ev.clientX, (int) ev.clientY });
        Observable<int[]> touchCoordinates$ = fromEvent(document, touchmove).map(ev -> ev.touches.item(0))
                .map(ev -> new int[] { (int) ev.clientX, (int) ev.clientY });
        Observable<Boolean> showCoordinates$ = fromEvent(input, change).map(ev -> Js.<HTMLInputElement>cast(ev.target))
                .mergeWith(just(input)).map(el -> el.checked);

        // combine them all (first line) and subscribe (second line)…
        showCoordinates$.switchMap(show -> show ? merge(mouseCoordinates$, touchCoordinates$) : just(new int[0]))
                .subscribe((int[] n) -> out.textContent = n.length == 2 ? n[0] + ", " + n[1] : "");
    }
}
