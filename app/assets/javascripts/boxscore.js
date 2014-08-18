function createHeader(parent, headings) {
    var header = parent.append("g");
    headings.forEach(function (h) {
        header.append("text").text(h.text)
            .attr("x", 10 + h.pos)
            .attr("y", "0")
            .attr("dy", "0.71em")
            .attr("class", "lineScore")
            .style("font-variant", "small-caps")
            .style("font-size", "80%");
    });
}

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

        var lineHeight = 40;

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
            var tSvg = teamDiv.append("svg").attr("height", 20 + lineHeight * t.playerStats.length).attr("width", "800");
            var headings = [
                {text: "Player", pos: 0},
                {text: "Pos", pos: 180},
                {text: "Min", pos: 210},
                {text: "Field Goals", pos: 240},
                {text: "Free Throws", pos: 240}

            ];
            createHeader(tSvg, headings);
            var players = tSvg.selectAll("g").data(t.playerStats.map(function (ps) {
                return shimPlayer(ps);
            })).enter().append("g").attr("transform", function (d, i) {
                return "translate( 10, " + (10 + i * lineHeight) + ")";
            });

            players.append("text")
                .attr("x", "0")
                .attr("y", "0")
                .attr("dy", "0.71em")
                .text(function (d) {
                    return d.name;
                })
                .attr("class", "lineScore");

            players.append("text")
                .attr("x", "180")
                .attr("y", "0")
                .attr("dy", "0.71em")
                .text(function (d) {
                    return d.pos;
                })
                .attr("class", "lineScore");
            playerMinutes(players, 225, (lineHeight - 6.0) / 2.0);

            var ggg = players.append("g").attr("transform","translate(260,0)");//.append("text").text(function(d,i){return d.fga;}).style("fill","white");
            ggg.selectAll("circle").data(function (d) {
                var arr = [];
                for (index = 0; index < d.fga; ++index) {
                    if (index < d.fgm) {
                        if (index < d.fg3m){
                            arr[index] = {x:15* index, y:0,color:"green", opacity:1.0};
                        } else {
                            arr[index] = {x:15 * index, y:0, color:"blue", opacity:1.0};
                        }
                    } else {
                        if ((index- d.fgm)< + d.fg3a ){
                            arr[index] = {x:15 *(index- d.fgm), y:15, color:"green", opacity:0.0};
                        } else {
                            arr[index] = {x:15 * (index- d.fgm), y:15, color:"blue", opacity:0.0};
                        }
                    }
                }
                return arr;
            }).enter().append("circle").attr("cx", function (d, i) {
                return d.x;
            }).attr("cy", function (d, i) {
                return d.y;
            }).attr("r", 7).style("stroke", function(d,i){return d.color;}).style("fill", function(d,i){return d.color;}).style("fill-opacity", function (d, i) {
                return d.opacity;
            });
//            makeCircles(players, 7.5, "white", 8).attr("transform", "translate(265,0)");
        });


    });
}

function playerMinutes(players, x, radius) {
    var arc = d3.svg.arc(x, radius / 2.1)
        .innerRadius(0)
        .outerRadius(function (d, i) {
            return radius;
        })
        .startAngle(function (d, i) {
            return 0;
        })
        .endAngle(function (d) {
            return 2 * 3.1415927 * d.minutes / 40.0;
        });
    players.append("g").attr("transform", "translate(" + x + ", " + radius / 2.1 + ")").append("path")
        .attr("d", arc)
        .attr("fill", "#f33");
    players.append("text")
        .attr("x", x)
        .attr("y", "0")
        .attr("text-anchor", "middle")
        .attr("dy", "0.71em")
        .text(function (d) {
            return d.minutes;
        })
        .attr("class", "lineScore")
    ;
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

function makeCircles(parent, radius, color, maxWidth) {
    var box = parent.selectAll("g.fgbox").data(function(d){return d;}).enter().append("g").attr("class", "fgbox");

    box.selectAll("circle").data(function (d, i) {
        var circleData = new Array(d.fga);
        for (var n = 0; n < d.fga; n++) {
            circleData[n] = {"x": (n % maxWidth) * (2 * radius + 1), "y": (Math.floor(n / maxWidth) * 2 * radius), "filled": n < d.fgm};
        }
        return circleData;
    }).enter().append("circle")
        .attr("cx", function (d, i) {
            return d.x;
        })
        .attr("cy", function (d, i) {
            return d.y;
        })
        .attr("r", radius)
        .attr("fill", color)
        .attr("stroke", color)
        .attr("fill-opacity", function (d, i) {
            if (d.fill) {
                return 1.0;
            } else {
                return 0.0;
            }
        });
    return box;
}