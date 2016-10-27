(defproject hammer-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[cljsjs/hammer "2.0.4-5"]
                 [compojure "1.5.1"]
                 [hiccup "1.0.5"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229" :scope "provided"]
                 [reagent "0.6.0"]
                 [reagent-forms "0.5.25"]
                 [reagent-utils "0.2.0"]
                 [ring "1.5.0"]
                 [ring-server "0.4.0"]
                 [ring/ring-defaults "0.2.1"]
                 [yogthos/config "0.8"]]

  :plugins [[lein-environ "1.0.2"]
            [lein-cljsbuild "1.1.1"]
            [lein-asset-minifier "0.2.7" :exclusions [org.clojure/clojure]]]

  :ring {:handler      hammer-cljs.handler/app
         :uberwar-name "hammer-cljs.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "hammer-cljs.jar"

  :main hammer-cljs.server

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets {:assets {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:min
                       {:source-paths ["src/cljs" "env/prod/cljs"]
                        :compiler
                                      {:output-to     "target/cljsbuild/public/js/app.js"
                                       :output-dir    "target/uberjar"
                                       :optimizations :advanced
                                       :pretty-print  false}}
                       :app
                       {:source-paths ["src/cljs" "env/dev/cljs"]
                        :compiler
                                      {:main          "hammer-cljs.dev"
                                       :asset-path    "/js/out"
                                       :output-to     "resources/public/js/app.js"
                                       :output-dir    "resources/public/js/out"
                                       :source-map    true
                                       :optimizations :none
                                       :pretty-print  true}}}}


  :figwheel {:http-server-root "public"
             :server-port      3449
             :nrepl-port       7002
             :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
             :css-dirs         ["resources/public/css"]
             :ring-handler     hammer-cljs.handler/app}


  :profiles {:dev     {:repl-options {:init-ns repl}

                       :dependencies [[com.cemerick/piggieback "0.2.2-SNAPSHOT"]
                                      [figwheel-sidecar "0.5.8"]
                                      [org.clojure/tools.nrepl "0.2.12"]
                                      [pjstadig/humane-test-output "0.8.1"]
                                      [prone "1.1.2"]
                                      [ring/ring-mock "0.3.0"]
                                      [ring/ring-devel "1.5.0"]]

                       :source-paths ["dev"]
                       :plugins      [[lein-figwheel "0.5.8"]]

                       :injections   [(require 'pjstadig.humane-test-output)
                                      (pjstadig.humane-test-output/activate!)]}

             :uberjar {:hooks        [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks   ["compile" ["cljsbuild" "once" "min"]]
                       :env          {:production true}
                       :aot          :all
                       :omit-source  true}})
