package wheelzoom.client;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.img;
import static org.jboss.gwt.elemento.core.EventType.wheel;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.HTMLElement;
import elemental2.dom.WheelEvent;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventCallbackFn;
import org.jboss.gwt.elemento.core.EventType;

/** https://stackoverflow.com/questions/46647138/zoom-in-on-a-mousewheel-point-using-scale-and-translate */
public class WheelZoom implements EntryPoint {

    @Override
    public void onModuleLoad() {
        HTMLElement target = img("https://source.unsplash.com/random")
                .style("max-width: 100vw; max-height: 100vh; transform-origin: 0 0;").get();
        Elements.body().style("height: 100vh; width: 100vw; overflow: hidden; margin: 0;").add(target);
        EventType.bind(document, wheel, new EventCallbackFn<WheelEvent>() {
            double[] pos = { 0, 0 };
            double scale = 1;

            @Override
            public void onEvent(WheelEvent ev) {
                ev.preventDefault();

                // cap the delta to [-1,1] for cross browser consistency
                double delta = Math.max(-1, Math.min(1, ev.deltaY / 200.));

                // determine the point on where the img is zoomed in
                double[] zoomTarget = { (ev.pageX - pos[0]) / scale, (ev.pageY - pos[1]) / scale };

                // calculate zoom and x and y based on it
                scale = Math.max(.1, Math.min(10, scale + delta * scale));
                pos[0] = -zoomTarget[0] * scale + ev.pageX;
                pos[1] = -zoomTarget[1] * scale + ev.pageY;

                target.style.transform = "translate(" + pos[0] + "px ," + pos[1] + "px) scale(" + scale + ")";
            }
        });
    }
}
