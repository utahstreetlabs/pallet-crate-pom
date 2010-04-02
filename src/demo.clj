(ns #^{:author "Hugo Duncan"}
  demo
  "A demo for pallet + jclouds.

  ;; First we load the demo package, and switch to the demo namespace
  (require 'demo)
  (in-ns 'demo)

  ;; Supported providers can be found with
  (supported-clouds)

  ;; We provide some credentials
  (def my-user \"your user\")
  (def my-password \"your api key\")

  ;; and log in to the cloud using the credentials defined above.
  ;; provider is a string specifiying the provider, as returned
  ;; from (supported-clouds)
  (def service (compute-service \"provider\" my-user my-password :log4j))

  ;; nodes can be listed with the nodes function
  (nodes service)

  ;; the compute service can also be bound
  (with-compute-service [service]
    (nodes))

  ;; or even
  (with-compute-service [\"provider\" my-user my-password :log4j]
    (nodes))

  ;; We can create a node, by specifying a name tag and a template.
  ;; webserver-template is a vector specifying features we want in
  ;; our image.
  (start-node service :webserver webserver-template)

  ;; At this point we can manage instance counts as a map.
  ;; e.g ensure that we have two webserver nodes
  (with-node-templates templates
    (converge service {:webserver 2}))

  ;; ... and we can remove our nodes
  (with-node-templates templates
    (converge service {:webserver 0}))

  ;; templates is a description of the images to use for each of
  ;; our node tags
  templates

  ;; Images are configured differently between clouds and os's.
  ;; Pallet comes with some \"crates\" that can be used to normalise
  ;; the images.  public-dns-if-no-nameserver ensures there is a
  ;; nameserver if none is already configured.
  ;; We probably want an admin user. The default is to use your current login
  ;; name, with no password and your id_rsa ssh key.
  (with-node-templates templates
    (converge service {:webserver 1}
      (bootstrap-with (public-dns-if-no-nameserver)
                      (bootstrap-admin-user))))

  ;; Bootstrapping is fine, but we might also want to configure the machines
  ;; with chef.
  (with-node-templates templates
    (converge service {:webserver 1}
      (bootstrap-with (public-dns-if-no-nameserver)
                      (bootstrap-admin-user)
                      (chef))
      (configure-with-chef user \"path_to_your_chef_repository\")))

  ;; and we can then run chef-solo at any time with
  (cook-nodes (nodes service) user \"path_to_your_chef_repository\")"
(:use [org.jclouds.compute :exclude [node-tag]]
      pallet.utils
      pallet.core
      pallet.chef
      pallet.package
      pallet.resource
      pallet.resource-apply
      pallet.compute
      pallet.crate.automated-admin-user
      pallet.crate.public-dns-if-no-nameserver
      pallet.bootstrap
      pallet.crate.rubygems
      pallet.crate.ruby
      pallet.crate.java
      pallet.crate.chef
      clj-ssh.ssh))

[id=1532, name=CentOS 5.3 (64-bit) w/ None, locationId=SANFRANCISCO, architecture=X86_64, osDescription=CentOS 5.3 (64-bit), osFamily=centos, version

(def centos-template [:centos :X86_64 :smallest
                      :os-description-matches ".*5.3.*"
                      :image-description-matches "[^gr]+"])
(def webserver-template [:ubuntu :X86_64 :smallest :os-description-matches "[^J]+9.10[^32]+"])
(def balancer-template (apply vector :inbound-ports [22 80] webserver-template))

(def #^{ :doc "This is a map defining node tag to instance template builder."}
     templates {:webserver webserver-template
                :balancer balancer-template
                :centos centos-template})

(def #^{ :doc "This is a map defining node tag to number of instances."}
     the-farm
     { :webserver 2 :balancer 1 })

