(ns personal-wiki.views.welcome
  (:require [personal-wiki.views.common :as common])
  (:use [noir.core :only [defpage]]))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to personal-wiki"]))
