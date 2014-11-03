var app = angular.module('team', ['ngResource']);
app.factory("Team", function ($resource) {
    return $resource("/api/team/:id");
});

app.controller('teamController', function ($scope, Team, key) {
    Team.get({id: key}, function (data) {
        $scope.team = data;
    });
});

app.controller('teamsController', function ($scope, Team) {
    Team.query(function (data) {
        $scope.teams = data;
    });
});