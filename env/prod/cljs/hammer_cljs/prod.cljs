(ns hammer-cljs.prod
  (:require [hammer-cljs.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
