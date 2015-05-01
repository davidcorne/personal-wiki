(ns personal-wiki.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css include-js html5]])
  )

(defpartial navigation-bar []
  [:div
   [:ul {:class "navigation-bar"}
    [:li [:a {:href "/notes"} "Notes"]]
    [:li [:a {:href "/new-note"} "New Note"]]
    ]
   ]
  )

(defpartial layout [& content]
  (html5
   [:head
    [:title "personal-wiki"]
    (include-js 
     "/js/commonmark.js"
     "/js/prism.js"
     "/js/notes.js")
    (include-css 
     "/css/reset.css"
     "https://kevinburke.bitbucket.org/markdowncss/markdown.css"
     "/css/prism.css"
     "/css/notes.css")
    ]
   [:body
    (navigation-bar)
    [:div#wrapper content]
    ]
   )
  )
