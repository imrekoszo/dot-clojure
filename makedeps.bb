#!/usr/bin/env bb

(require '[clojure.edn :as edn]
         '[clojure.set :as set])

(def deps
  (-> *command-line-args*
      (first)
      (or "deps.edn")
      (slurp)
      (edn/read-string)))

(def secret
  (try
    (-> *command-line-args*
        (second)
        (or "secret-deps.edn")
        (slurp)
        (edn/read-string))
    (catch Exception _)))

(def all-deps (merge-with merge deps secret))

(def alias-recipes
  (let [lite #{:bench :dev :hashp :nrebl :rebl-8 :trace}]
    {:cursive-lite (-> lite sort)
     :cursive-full (-> lite
                       (set/union
                        #{:decompile :kaocha :measure :reflect :speculative})
                       (sort))}))

(defn recipe->alias [recipe]
  (-> all-deps
      (:aliases)
      ((apply juxt recipe))
      (->> (reduce (fn [acc m] (merge-with merge acc m))))
      (select-keys [:extra-deps])))

(update all-deps :aliases
        #(->> alias-recipes
              (into % (map (juxt key (comp recipe->alias val))))))
