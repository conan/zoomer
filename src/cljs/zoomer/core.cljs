(ns zoomer.core
  (:require [cljsjs.hammer]
            [reagent.core :as r :refer [atom]]))


(defn transform
  "Generates a cross-browser style for performing efficient CSS transforms"
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
         (let [mc (new js/Hammer.Manager (r/dom-node this))]
           ;; Doubletap
           (js-invoke mc "add" (new js/Hammer.Tap #js{"event" "doubletap" "taps" 2}))
           (js-invoke mc "on" "doubletap" #(if (= 1 (:scale @!zoom))
                                            (swap! !zoom assoc :scale 2)
                                            (swap! !zoom assoc :scale 1)))
           ;; Pan
           (js-invoke mc "add" (new js/Hammer.Pan #js{"direction" js/Hammer.DIRECTION_ALL "threshold" 0}))
           (js-invoke mc "on" "panstart" #(reset! !start-zoom @!zoom))
           (js-invoke mc "on" "pan" #(let [{:keys [x y]} @!start-zoom]
                                      (swap! !zoom assoc :x (+ x (.-deltaX %))
                                                         :y (+ y (.-deltaY %)))))
           ;; Pinch
           (js-invoke mc "add" (new js/Hammer.Pinch))
           (js-invoke mc "on" "pinchstart" #(do (reset! !start-zoom @!zoom)
                                                (.preventDefault %)))
           (js-invoke mc "on" "pinchmove" #(let [{:keys [x y scale]} @!start-zoom]
                                            (reset! !zoom {:x     (+ x (.-deltaX %))
                                                           :y     (+ y (.-deltaY %))
                                                           :scale (* scale (.-scale %))})
                                            (.preventDefault %)))
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


(defn init! []
  (r/render [zoomer] (.getElementById js/document "app")))
