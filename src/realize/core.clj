(ns realize.core)

(defn guard-against-infinite-lazy [ns max-len]
  (when (= (inc max-len) (bounded-count (inc max-len) ns))
    (throw (ex-info (str "Sequence of > " max-len " items found, aborting to guard against infinite seqs!")
                    {:max-len max-len
                     :first-items (take (min max-len 10) ns)}))))

(defn realize [form & [max-len]]
  (try
    (cond
      (list? form) (apply list (map realize form))
      (instance? clojure.lang.IMapEntry form) (vec (map realize form))
      (seq? form) (do (guard-against-infinite-lazy form (or max-len 10000))
                      (doall (map realize form)))
      (instance? clojure.lang.IRecord form) (reduce (fn [r x] (conj r (realize x))) form form)
      (coll? form) (if (= form (empty form))
                     form ;; unable to empty, so cannot recreate -> skip
                     (into (empty form) (map realize form)))
      :else form)
    (catch Throwable e {::exception e})))

(defn find-exceptions
  ([form] (find-exceptions form []))
  ([form path]
   (try
     (cond
       (and (map? form) (::exception form)) [{:exception (::exception form) :path path}]
       (map? form) (mapcat (fn [[k v]]
                             (concat
                              (find-exceptions k path)
                              (find-exceptions v (conj path k))))
                           form)
       (coll? form) (mapcat (fn [i v] (find-exceptions v (conj path i)))
                            (range)
                            form))
     (catch Throwable e
       [{:exception (Exception. "Realize got an exception when finding exceptions, isn't that something!" e)
         :path []}]))))
