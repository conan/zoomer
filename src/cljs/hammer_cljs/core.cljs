(ns hammer-cljs.core
  (:require [cljsjs.hammer]
            [reagent.core :as r :refer [atom]]))


(defn transform
  "Generates a cross-browser style for performing efficient CSS
  transforms"
  [{:keys [x y scale]}]
  (let [transform (str "translate3d(" x "px, " y "px, 0px) scale(" scale "," scale ")")]
    {:WebkitTransform transform
     :MozTransform    transform
     :transform       transform}))


(defn zoomer
  []
  (let [!hammer-manager (atom nil)
        !zoom           (atom {:x 0 :y 0 :scale 1})
        !start-zoom     (atom {:x 0 :y 0 :scale 1})]
    (r/create-class
      {:component-did-mount
       (fn [this]
         (let [mc    (new js/Hammer.Manager (r/dom-node this) #js{"preventDefault" true})
               tap   (new js/Hammer.Tap #js{"event" "doubletap" "taps" 2})
               pinch (new js/Hammer.Pinch #js{"enable" true})
               pan   (new js/Hammer.Pan #js{"direction" js/Hammer.DIRECTION_ALL "threshold" 0})]
           (doto mc
             (js-invoke "add" tap)
             (js-invoke "add" pinch)
             (js-invoke "add" pan)
             (js-invoke "on" "doubletap" #(do (if (= 1 (:scale @!zoom))
                                                (swap! !zoom assoc :scale 2)
                                                (swap! !zoom assoc :scale 1))
                                              (.preventDefault %)))
             (js-invoke "on" "pinchstart" #(do (reset! !start-zoom @!zoom)
                                               (.preventDefault %)))
             (js-invoke "on" "pinchmove" #(let [{:keys [x y scale]} @!start-zoom]
                                           (reset! !zoom {:x     (+ x (.-deltaX %))
                                                          :y     (+ y (.-deltaY %))
                                                          :scale (* scale (.-scale %))})
                                           (.preventDefault %)))
             (js-invoke "on" "panstart" #(do (reset! !start-zoom @!zoom)
                                             (.preventDefault %)))
             (js-invoke "on" "pan" #(do (let [{:keys [x y]} @!start-zoom]
                                          (swap! !zoom assoc :x (+ x (.-deltaX %))
                                                             :y (+ y (.-deltaY %))))
                                        (.preventDefault %))))
           (reset! !hammer-manager mc)))

       :reagent-render
       (fn [_]
         [:div.zoomer
          [:img {:src   "/images/clojure-logo.png"
                 :style (transform @!zoom)}]])

       :component-will-unmount
       (fn [_]
         (when-let [mc @!hammer-manager]
           (js-invoke mc "destroy")))})))


(defn mount-root []
  (r/render [zoomer] (.getElementById js/document "app")))


(defn init! []
  (mount-root))
