# compologin

[![Build Status](https://semaphoreci.com/api/v1/danimal/compologin/branches/master/badge.svg)](https://semaphoreci.com/danimal/compologin)
[![Code Climate](https://codeclimate.com/github/d4goxn/compologin/badges/gpa.svg)](https://codeclimate.com/github/d4goxn/compologin)

A Facebook OAuth login demo

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed, and a Facebook app.

[leiningen]: https://github.com/technomancy/leiningen

## Run

To start a demo web server for the application, run:

    APP_ID=your-fb-app-id APP_SECRET=your-fb-app-secret lein ring server

Go to http://localhost:3000 and log in. You will see a dump of your graph API access token info.

## Test

Create a test app, and use it's ID and secret.

    APP_ID=your-fb-app-id APP_SECRET=your-fb-app-secret lein cloverage
