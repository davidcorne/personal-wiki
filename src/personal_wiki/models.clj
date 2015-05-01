(ns personal-wiki.models
  (:require 
   [monger.core :as monger]
   [personal-wiki.models.note :as note]
   )
  )

;==============================================================================
(defn- initialise-local []
  (println "Connecting to local dev db.")
  (->
   (monger/connect)
   (monger/get-db "notes")
   )
  )
  
;==============================================================================
(defn- initialise-uri [uri]
  (println "Connecting to:" uri)
  (->
   (monger/connect-via-uri uri)
   (get :db)
   )
  )

;==============================================================================
(defn initialise [uri]
  (let [db (if (nil? uri) (initialise-local)(initialise-uri uri))]
    (note/initialise db)
    )
  )

