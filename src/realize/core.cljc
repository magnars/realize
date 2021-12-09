(ns realize.core)

(defn guard-against-infinite-lazy [ns max-len]
  (when (= (inc max-len) (bounded-count (inc max-len) ns))
    (throw (ex-info (str "Sequence of > " max-len " items found, aborting to guard against infinite seqs!")
                    {:max-len max-len
                     :first-items (take (min max-len 10) ns)}))))

(defn realize
  ([form]
   (realize form {}))
  ([form opt-or-max-len]
   (let [opt (if (number? opt-or-max-len)
               {:max-len opt-or-max-len}
               opt-or-max-len)
         realize-1 #(realize % opt)
         max-len (or (:max-len opt) 10000)]
     (try
       (cond
         (list? form) (apply list (map realize-1 form))
         (map-entry? form) (vec (map realize-1 form))
         (seq? form) (do (when-not (:tolerate-long-seqs? opt)
                           (guard-against-infinite-lazy form max-len))
                         (let [res (->> form
                                        (take (inc max-len))
                                        (map realize-1)
                                        doall)]
                           (if (< max-len (count res))
                             (with-meta (drop-last 1 res) {::truncated? true})
                             res)))
         (record? form) (reduce (fn [r x] (conj r (realize-1 x))) form form)
         (coll? form) (if (= form (empty form))
                        form ;; unable to empty, so cannot recreate -> skip
                        (into (empty form) (map realize-1 form)))
         :else form)
       (catch #?(:clj Throwable
                 :cljs :default) e {::exception e})))))

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
     (catch #?(:clj Throwable
               :cljs :default) e
       [{:exception (ex-info "Realize got an exception when finding exceptions, isn't that something!" {:path path} e)
         :path []}]))))
