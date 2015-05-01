(ns personal-wiki.models.note
  (:require 
   [monger.collection :as mc]
   [monger.operators :refer :all]
   )
  (:import [org.bson.types ObjectId]))

(def note-collection "notecollection")

;==============================================================================
(defn upgrade-database! [db]
  (let [notes (mc/find-maps db note-collection {})
        predicate #(not (contains? % :datetime-created))
        notes-without-creation (filter predicate notes)
        ids-to-update (map #(get % :_id) notes-without-creation)
        now (new java.util.Date)
        ]
    (mc/update-by-ids
     db
     note-collection
     ids-to-update
     {$set {:datetime-created now}}
     )
    )
  )

;==============================================================================
(defn initialise [db]
  (def db db)
  (upgrade-database! db)
  )

;==============================================================================
(defn- get-notes-by-title [title]
  (mc/find-maps db note-collection {:title title})
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
  (mc/find-maps db note-collection {})
  )

;==============================================================================
(defn upgrade-collection! []
  )
;==============================================================================
(defn add! [note]
  (let [current-datetime (new java.util.Date)
        id (ObjectId.)
        final (assoc note :_id id :datetime-created current-datetime)]
    (println final)
    (mc/insert db note-collection final)
    ))

;==============================================================================
(defn update! [title body]
  (let [old-note (get-note title)
        note (assoc old-note :body body)]
    (mc/update-by-id db note-collection (get old-note :_id) note)
    )
  )

;==============================================================================
(defn rename! [old-title new-title]
  (mc/update db note-collection {:title old-title} {$set {:title new-title}})
  )

;==============================================================================
(defn remove! [title]
  (mc/remove db note-collection {:title title})
  )
