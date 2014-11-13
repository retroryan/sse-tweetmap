var app = angular.module('myApp', ["leaflet-directive"]);

app.factory('Twitter', function($http, $timeout) {

    var feed = {};

    var twitterService = {
        tweets: [],
        query: function (query) {
            feed = new EventSource("/tweetFeed?q=" + searchString);
            feed.addEventListener('message', tweetFeedCallback, false);
        }
    };

    var tweetFeedCallback = function(event) {
        console.log(event);
        twitterService.tweets = JSON.parse(event.data).statuses;
    };

    var searchString = "Scala";
    feed = new EventSource("/tweetFeed?query=" + searchString);
    feed.addEventListener('message', tweetFeedCallback, false);

    return twitterService;
});

app.controller('Search', function($scope, $http, $timeout, Twitter) {

    $scope.search = function() {
        Twitter.query($scope.query);
    };

});

app.controller('Tweets', function($scope, $http, $timeout, Twitter) {

    $scope.tweets = [];
    $scope.markers = [];
    
    $scope.$watch(
        function() {
            return Twitter.tweets;
        },
        function(tweets) { 
            $scope.tweets = tweets;
            
            $scope.markers = tweets.map(function(tweet) {
                return {
                    lng: tweet.coordinates.coordinates[0],
                    lat: tweet.coordinates.coordinates[1],
                    message: tweet.text,
                    focus: true
                };
            });
        }
    );
    
});