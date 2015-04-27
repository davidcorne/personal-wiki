(ns personal-wiki.server
  (:require [noir.server :as server]
            [personal-wiki.models :as models]))

(server/load-views-ns 'personal-wiki.views)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (models/initialise)
    (server/start port {:mode mode
                        :ns 'personal-wiki})))

