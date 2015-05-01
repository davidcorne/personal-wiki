(ns personal-wiki.views.notes
  (:use noir.core
        ; <nnn> [ring.util.codec :only [percent-encode percent-decode]]
        [hiccup.page :only [include-js]]
        [hiccup.element :only [javascript-tag]])
  (:require [noir.validation :as vali])
  (:require [noir.response :as resp])
  (:require [personal-wiki.views.common :as common])
  (:require [hiccup.form :as form])
  ;(:require [ring/ring-codec :as ring])
  (:require [personal-wiki.models.note :as model])
  )

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
(defn- note-valid? [{:keys [title body]}]
  (vali/rule 
   (model/valid-title? title)
   [:title (str "Title \"" title "\" already taken.")]
   )
  (not (vali/errors? :title :body))
  )

;==============================================================================
(defpartial display-note-title-buttons [title]
  [:div {:class "title-buttons"}
   [:form {:action (str "/note/edit/" title)}
    (form/submit-button "Edit")
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
  [:h1 {:class "title"} 
   [:a {:href (str "/note/" title)} title]
   (title-buttons)
   ]
  )

;==============================================================================
(defpartial display-note-body [body]
   [:div {:class "note-body" }]
   [:script
    (->
     (clojure.string/escape body {\' "\\'" \return "" \newline "\\n"})
     (markdown-conversion-javascript)
     )
    ]
   )

;==============================================================================
(defpartial display-note-fields [{:keys [title body]}]
  [:div {:class "note"}
    (display-note-title title (fn [] (display-note-title-buttons title)))
    (display-note-body body)
   ])

;==============================================================================
(defpartial display-edit-note-body [title body]
  [:div {:class "note-edit-body"}
   (form/form-to 
    [:post 
     (str 
      "/note/edit/" 
      (clojure.string/escape title {\space "%20"})
      )
     ]
    [:textarea {:name "body"} body]
    (display-note-body body)
    [:div {:class "edit-buttons"}
     (form/submit-button "Save")
     (form/reset-button "Reset")
     ]
    )
   ]
  )

;==============================================================================
(defpartial display-edit-note [{:keys [title body]}]
  [:div {:class "note"}
   [:div {:class "note-edit"}
    (display-note-title title (fn [] ))
    (display-edit-note-body title body)
    ]
   ]
  )

;==============================================================================
(defpage "/new-note" {:as note}
  (common/layout
   (form/form-to 
    {:class "new-note-form"}
    [:post "/new-note"]
    (input-note-fields note)
    (form/submit-button "Add note"))))

;==============================================================================
(defpage [:post "/new-note"] {:as note}
  (if (note-valid? note)
    (do
      (model/add! note)
      (resp/redirect (str "/note/" (get note :title)))
      )
    (render "/new-note" note)
    )
  )

;==============================================================================
(defpage [:post "/delete-note"] {:as data}
  (model/remove! (get data :title))
  (resp/redirect "/")
  )

;==============================================================================
(defpage "/note/edit/:title" {:keys [title]}
  (common/layout
   (display-edit-note (model/get-note title))
   ))

;==============================================================================
(defpage [:post "/note/edit/:title"] {:as data}
  (model/update! data)
  (resp/redirect (str "/note/" (get data :title)))
  )

;==============================================================================
(defpage "/note/:title" {:keys [title]}
  (common/layout
   (display-note-fields (model/get-note title))
   ))

;==============================================================================
(defpage "/notes" {}
  (common/layout
   (map display-note-fields (model/get-notes))
   ))

;==============================================================================
(defpage "/" {}
  (resp/redirect "/notes"))
