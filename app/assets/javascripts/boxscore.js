function loadBox(url) {
    d3.json("testbed", function (err, data) {
        var teamMetaData = {};
        data.meta.teams.forEach(
            function (x) {
                teamMetaData[x.id] = {shortName: x.shortName, nickName: x.nickName, color: x.color};
            });
        var teams = data.teams;
        var mainBox = d3.select("#main-box");
        mainBox.selectAll("div").data(teams).enter()
            .append("div").attr("class", "team-box").attr("id", function (d, i) {
                return "team-" + d.teamId;
            });


        teams.forEach(function (t) {

//                firstName: "Talbott",
//                lastName: "Denny",
//                position: "G",
//                minutesPlayed: "23",
//                fieldGoalsMade: "2-3",
//                threePointsMade: "1-1",
//                freeThrowsMade: "2-2",
//                totalRebounds: "1",
//                offensiveRebounds: "0",
//                assists: "0",
//                personalFouls: "5",
//                steals: "0",
//                turnovers: "0",
//                blockedShots: "0",
//                points: "7"
            var teamDiv = d3.select("div#team-" + t.teamId);
            teamDiv.append("h3").text(teamMetaData[t.teamId].shortName + " " + teamMetaData[t.teamId].nickName);
            var tSvg = teamDiv.append("svg").attr("height", 20 + 40 * t.playerStats.length).attr("width", "800");
            var players = tSvg.selectAll("g").data(t.playerStats.map(function (ps) {
                return shimPlayer(ps);
            })).enter().append("g").attr("transform",function(d, i){return "translate( 10, "+(10+i*40)+")";});
            var playerTextColor="#eef";
            var playerTextSize="19px";
            var playerTextFont="Verdana, Helvetica, Arial, sans-serif";
            players.append("text")
                .attr("x", "0")
                .attr("y", "0")
                .attr("dy", "0.71em")
                .text(function (d) {
                    return d.name;
                })
                .attr("font-family", playerTextFont)
                .attr("font-size", playerTextSize)
                .attr("fill", playerTextColor);

            players.append("text")
                .attr("x", "180")
                .attr("y", "0")
                .attr("dy", "0.71em")
                .text(function (d) {
                    return d.pos;
                })
                .attr("font-family", playerTextFont)
                .attr("font-size", playerTextSize)
                .attr("fill", playerTextColor);


            players.append("circle")
                .attr("cx", function (d, i) {
                    return 225;
                })
                .attr("cy", function (d, i) {
                    return 7.5;
                }).attr("r","15")
                .attr("fill","#aaf");

            players.append("text")
                .attr("x", "225")
                .attr("y", "0")
                .attr("text-anchor", "middle")
                .attr("dy", "0.71em")
                .text(function (d) {
                    return d.minutes;
                })
                .attr("font-family", playerTextFont)
                .attr("font-size", playerTextSize)
                .attr("fill", playerTextColor);

        });


    });
}

function shimPlayer(ps) {
    var name = ps.firstName + " " + ps.lastName;
    var pos = ps.position;
    var minutes = ps.minutesPlayed;
    var fg = ps.fieldGoalsMade.split("-");
    var fg3 = ps.threePointsMade.split("-");
    var ft = ps.freeThrowsMade.split("-");
    var fga = fg[1];
    var fgm = fg[0];
    var fg3a = fg3[1];
    var fg3m = fg3[0];
    var fta = ft[1];
    var ftm = ft[0];
    return {"name": name, "pos": pos, "minutes": minutes, "fga": fga, "fgm": fgm, "fg3a": fg3a, "fg3m": fg3m, "fta": fta, "ftm": fta};
}