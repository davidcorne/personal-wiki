(ns personal-wiki.models.note
  (:require 
   [monger.core :as mg]
   [monger.collection :as mc]
   [monger.operators :refer :all]
   [personal-wiki.models :as models]
   )
  (:import [org.bson.types ObjectId]))

;==============================================================================
(defn- get-notes-by-title [title]
  (mc/find-maps models/db models/note-collection {:title title})
  )

;==============================================================================
(defn valid-title? [title]
  (empty? (get-notes-by-title title))
  )

;==============================================================================
(defn get-note [title]
  (->
   (get-notes-by-title title)
   (first)
   ))

;==============================================================================
(defn get-notes []
  (mc/find-maps models/db models/note-collection {})
  )

;==============================================================================
(defn add! [note]
  (let [final (assoc note :_id (ObjectId.))]
    (println final)
    (mc/insert models/db models/note-collection final)
    ))

;==============================================================================
(defn update! [note]
  (let [old-note (get-note (get note :title))]
    (mc/update-by-id models/db models/note-collection (get old-note :_id) note)
    )
  )

;==============================================================================
(defn rename! [old-title new-title]
  (mc/update 
   models/db
   models/note-collection
   {:title old-title}
   {$set {:title new-title}}
   )
  )

;==============================================================================
(defn remove! [title]
  (mc/remove models/db models/note-collection {:title title})
  )
