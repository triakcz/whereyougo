package menion.android.whereyougo.maps.mapsforge.overlay;

import java.util.HashMap;

import menion.android.whereyougo.maps.mapsforge.TapEventListener;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.Projection;
import org.mapsforge.android.maps.overlay.ListOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.model.GeoPoint;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

/**
 * Created by biylda on 30.11.13.
 */
public class PointListOverlay extends ListOverlay {
  HashMap<GeoPoint, PointOverlay> hitMap = new HashMap<GeoPoint, PointOverlay>();
  TapEventListener onTapListener;

  public PointListOverlay() {
    super();
  }

  public synchronized void clear() {
    getOverlayItems().clear();
    hitMap.clear();
  }

  public synchronized boolean checkItemHit(GeoPoint geoPoint, MapView mapView) {
    Log.e("litezee", "check hit " + geoPoint.latitude + " " + geoPoint.longitude);
    Projection projection = mapView.getProjection();
    Point eventPosition = projection.toPixels(geoPoint, null);

    // check if the translation to pixel coordinates has failed
    if (eventPosition == null) {
      return false;
    }

    Point checkItemPoint = new Point();
    for (int i = this.getOverlayItems().size() - 1; i >= 0; i--) {
      OverlayItem item = getOverlayItems().get(i);

      if (!(item instanceof PointOverlay)) {
        continue;
      }
      PointOverlay checkOverlayItem = (PointOverlay) item;

      // make sure that the current item has a position
      if (checkOverlayItem.getGeoPoint() == null) {
        continue;
      }

      checkItemPoint = projection.toPixels(checkOverlayItem.getGeoPoint(), checkItemPoint);
      // check if the translation to pixel coordinates has failed
      if (checkItemPoint == null) {
        continue;
      }

      // select the correct marker for the item and get the position
      Rect checkMarkerBounds = checkOverlayItem.getDrawable().getBounds();
      if (checkMarkerBounds.left == checkMarkerBounds.right
          || checkMarkerBounds.top == checkMarkerBounds.bottom)
        continue;

      // calculate the bounding box of the marker
      int checkLeft = checkItemPoint.x + checkMarkerBounds.left;
      int checkRight = checkItemPoint.x + checkMarkerBounds.right;
      int checkTop = checkItemPoint.y + checkMarkerBounds.top;
      int checkBottom = checkItemPoint.y + checkMarkerBounds.bottom;

      // check if the event position is within the bounds of the marker
      if (checkRight >= eventPosition.x && checkLeft <= eventPosition.x
          && checkBottom >= eventPosition.y && checkTop <= eventPosition.y) {
        if (onTap(checkOverlayItem)) {
          hitMap.put(geoPoint, checkOverlayItem);
          return true;
        }
      }
    }
    return false;
  }

  public synchronized void onTap(GeoPoint p) {
    int i = hitMap.remove(p).getId();
    Log.d("MAP", "tapped " + i);
  }

  public synchronized boolean onTap(PointOverlay pointOverlay) {
    Log.d("MAP", "tapped bool " + pointOverlay.getId());
    if (onTapListener != null)
      onTapListener.onTap(pointOverlay);
    return true;
  }

  public void registerOnTapEvent(TapEventListener onTapListener) {
    this.onTapListener = onTapListener;
  }

  public void unregisterOnTapEvent() {
    this.onTapListener = null;
  }
}
