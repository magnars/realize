(defproject realize "1.0.0"
  :description "Realizing clojure data structures, no more laziness"
  :url "https://github.com/magnars/realize"
  :license {:name "BSD-3-Clause"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :profiles {:dev {:plugins []
                   :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                                  [org.clojure/data.generators "0.1.2"]
                                  [org.clojure/test.generative "0.5.2"]]
                   :source-paths ["dev"]}})
