$(document).ready(
    function () {
        $("#shortener").submit(
            async function (event) {
                event.preventDefault();
                let data = $(this).serializeArray()

                const coords = await getCoords();
                if (coords !== undefined) {
                    data.push({name: "lat", value: coords.lat})
                    data.push({name: "lon", value: coords.lon})
                }

                $.ajax({
                    type: "POST",
                    url: "/api/link",
                    data: $.param(data),
                    success: function (data, status) {
                        $("#result").html(
                            "<div class='alert alert-success lead'><a target='_blank' href='"
                            + data['url']
                            + "'>"
                            + data['url']
                            + "</a></div>");
                    },
                    error: function (response, status) {
                        $("#result").html(
                            "<div class='alert alert-danger lead'>"
                            + "ERROR:" + "<br>"
                            + response['responseJSON']['message']
                            + "</div>");
                    }
                });
            });
    });
                // Funcion de FJ
            /*function (event) {
                event.preventDefault();
                $.ajax({
                    type: "POST",
                    url: "/api/link",
                    data: $(this).serialize(),
                    success: function (msg, status, request) {
                        $("#result").html(
                            "<div class='alert alert-success lead'><a target='_blank' href='"
                            + request.getResponseHeader('Location')
                            + "'>"
                            + request.getResponseHeader('Location')
                            + "</a></div>");
                    },
                    error: function () {
                        $("#result").html(
                            "<div class='alert alert-danger lead'>ERROR</div>");
                    }
                });*/

const getCoords = async () => {
    const pos = await new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject);
    }).catch(e => {
        //Error
        console.error("Error: " + e);
    });
    // Allow pressed
    if (pos !== undefined) {
        return {
            lat: pos.coords.latitude,
            lon: pos.coords.longitude,
        }
    }
}

/*
async function getLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(getLocationByCoord, handleError);
    } else {
        console.error("Geolocation is not supported by this browser.");
    }
}


function handleError(error) {
    let errorStr
    switch (error.code) {
        case error.PERMISSION_DENIED:
            errorStr = 'User denied the request for Geolocation.'
            break
        case error.POSITION_UNAVAILABLE:
            errorStr = 'Location information is unavailable.'
            break
        case error.TIMEOUT:
            errorStr = 'The request to get user location timed out.'
            break
        default:
            errorStr = 'An unknown error occurred.'
    }
    console.error('Error occurred: ' + errorStr)
}

function getLocationByCoord(position) {
    //data: {"lat":position.coords.latitude, "long":position.coords.longitude},
    data['lat'] = position.coords.latitude
    data['lon'] = position.coords.longitude
}
*/

/*
function getLocationByIP() {
    $.ajax({
        type: "POST",
        url: "/api/locationByIP",
        success: function (data, status) {
            console.log(data);
            console.log(status);
            $("#result").html(
                "<div class='alert alert-success lead'>"
                + "Tu ubicación por IP: " + data['city'] + " " + data['country']
                + "</div>");
        },
        error: function () {
            $("#result").html(
                "<div class='alert alert-danger lead'>ERROR</div>");
        }
    })
}*/