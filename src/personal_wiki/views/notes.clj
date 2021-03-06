(ns personal-wiki.views.notes
  (:use noir.core
        [hiccup.page :only [include-js]]
        [hiccup.element :only [javascript-tag]])
  (:require [noir.validation :as vali])
  (:require [noir.response :as resp])
  (:require [personal-wiki.views.common :as common])
  (:require [hiccup.form :as form])
  (:require [personal-wiki.models.note :as model])
  )

;==============================================================================
; Private utility functions.

;==============================================================================
(defn- markdown-conversion-javascript [markdown]
  (str 
   "notes.setHtml('"
   markdown
   "');"
   )
  )

;==============================================================================
(defn- delete-note-confirmation-javascript [title]
  (str 
   "return confirm('Are you sure you want to delete \""
   title
   "\"?')"
   )
)  

;==============================================================================
(defn- note-title-valid? [title]
  (vali/rule 
   (model/valid-title? title)
   [:title (str "Title \"" title "\" already taken.")]
   )
  (not (vali/errors? :title :body))
  )
;==============================================================================
(defn- sanitise-note-title [title]
  (clojure.string/trim title)
  )

;==============================================================================
(defn- note-valid? [{:keys [title body]}]
  (note-title-valid? title)
  (not (vali/errors? :title :body))
  )

;==============================================================================
(defn- note-valid? [{:keys [title body]}]
  (vali/rule 
   (model/valid-title? title)
   [:title (str "Title \"" title "\" already taken.")]
   )
  (not (vali/errors? :title :body))
  )

;==============================================================================
; Partials.

;==============================================================================
(defpartial error-item [[first-error]]
  [:p.error first-error])

;==============================================================================
(defpartial input-note-fields [{:keys [title body]}]
  (vali/on-error :title error-item)
  (form/label "title" "Title: ")
  (form/text-field {:placeholder "Title"} "title" title)
  (form/label "body" "Body: ")
  (form/text-area "body" body))

;==============================================================================
(defpartial display-note-title-buttons [title]
  [:div.title-buttons
   [:form {:action (str "/note/edit/" title)}
    (form/submit-button "Edit")
    ]
   [:form {:action (str "/note/rename/" title)}
    (form/submit-button "Rename")
    ]
   (form/form-to 
    {:onsubmit (delete-note-confirmation-javascript title)}
    [:post "/delete-note"]
    (form/hidden-field "title" title)
    (form/submit-button "Delete"))
   ]
  )

;==============================================================================
(defpartial display-note-title [title title-buttons]
  [:h1.title
   [:a {:href (str "/note/" title)} title]
   (title-buttons)
   ]
  )

;==============================================================================
(defpartial display-note-body [body]
   [:div.note-body]
   [:script
    (println body)
    (->
     (clojure.string/escape body {\' "\\'" \return "" \newline "\\n"})
     (markdown-conversion-javascript)
     )
    ]
    )

;==============================================================================
(defpartial display-note-fields [{:keys [title body]}]
  [:div.note
    (display-note-title title (fn [] (display-note-title-buttons title)))
    (display-note-body body)
   ])

;==============================================================================
(defpartial display-edit-note-body [title body]
  [:div.note-edit-body
   (form/form-to 
    [:post 
     (str 
      "/note/edit/" 
      (clojure.string/escape title {\space "%20"})
      )
     ]
    [:textarea {:name "body"} body]
    (display-note-body body)
    [:div.edit-buttons
     (form/submit-button "Save")
     (form/reset-button "Reset")
     ]
    )
   ]
  )

;==============================================================================
(defpartial display-edit-note [{:keys [title body]}]
  [:div.note
   [:div.note-edit
    (display-note-title title (fn [] ))
    (display-edit-note-body title body)
    ]
   ]
  )

;==============================================================================
(defpartial display-rename-note [{:keys [title body]}]
  [:div.note
    [:h1.title
     (form/form-to
      [:post 
       (str 
        "/note/rename/" 
        (clojure.string/escape title {\space "%20"})
        )
       ]
      (form/hidden-field "old-title" title)
      (vali/on-error :title error-item)
      (form/label "title" "New Title: ")
      (form/text-field "new-title" title)
      (form/submit-button "Rename")
      )
     ]
    (display-note-body body)
   ]
  )

;==============================================================================
; Routes through the app.

;==============================================================================
(defpage new-note "/new-note" {:as note}
  (common/layout
   (form/form-to 
    {:class "new-note-form"}
    [:post "/new-note"]
    (input-note-fields note)
    (form/submit-button "Add note"))))

;==============================================================================
(defpage [:post "/new-note"] {:as raw-note}
  (let [note (update-in raw-note [:title] sanitise-note-title)]
    (if (note-valid? note)
      (do
        (model/add! note)
        (resp/redirect (str "/note/" (:title note)))
        )
      (render "/new-note" note)
      )
    )
  )

;==============================================================================
(defpage delete-note [:post "/delete-note"] {:as data}
  (model/remove! (:title data))
  (resp/redirect "/")
  )

;==============================================================================
(defpage edit-note "/note/edit/:title" {:keys [title]}
  (common/layout
   (display-edit-note (model/get-note title))
   ))

;==============================================================================
(defpage [:post "/note/edit/:title"] {:keys [title body]}
  (model/update! title body)
  (resp/redirect (str "/note/" title))
  )

;==============================================================================
(defpage rename-page "/note/rename/:title" {:keys [title]}
  (common/layout
   (display-rename-note (model/get-note title))
   ))

;==============================================================================
(defpage [:post "/note/rename/:title"] {:as data}
  (let [old-title (:old-title data)
        new-title (sanitise-note-title (:new-title data))]
    (if (note-title-valid? new-title)
      (do
        (model/rename! old-title new-title)
        (resp/redirect (str "/note/" new-title))
        )
      (do
        ; <nnn> Should use render and use the error, couldn't get it to work.
        (resp/redirect (str "/note/rename/" old-title))
        )
      )
    )
  )

;==============================================================================
(defpage note-page "/note/:title" {:keys [title]}
  (println title)
  (if (model/note-exists? title)
    (do
      (common/layout
       (println title)
       (println (model/get-notes))
       (display-note-fields (model/get-note title))
       ))
    (render (str "/note-not-found/" title))
    )
  )

;==============================================================================
(defpage note-not-found "/note-not-found/:title" {:keys [title]}
  (common/layout
   [:p (str "Sorry, the note \"" title "\" does not exist.")]
   )
  )

;==============================================================================
(defpage notes-page "/notes" {}
  (common/layout
   (let [notes (model/get-notes)]
     (println notes)
     (map display-note-fields notes)
     )
   )
  )

;==============================================================================
(defpage home-page "/" {}
  (resp/redirect "/notes"))
