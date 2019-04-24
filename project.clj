(defproject realize "2019-04-24"
  :description "Realizing clojure data structures, no more laziness"
  :url "https://github.com/magnars/realize"
  :license {:name "BSD-3-Clause"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :profiles {:dev {:plugins []
                   :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                                  [org.clojure/data.generators "0.1.2"]
                                  [org.clojure/test.generative "0.5.2"]
                                  [com.datomic/datomic-free "0.9.5544" :exclusions [org.clojure/tools.cli]]]
                   :source-paths ["dev"]}
             :kaocha {:dependencies [[lambdaisland/kaocha "0.0-418"]]}}
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]})
