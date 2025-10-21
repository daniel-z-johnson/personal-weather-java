@import self.exercise.personal_weather.models.GeoLocation
@import java.util.List

@param String title
@param List<GeoLocation> locs

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
</head>
<body>
@template.common.navbar()

<main>
    <h1>Welcome to my homepage!</h1>
    <p>This is the content of the home page.</p>
    @for(var loc : locs)
        <h4>${loc.getCity()}</h4>
    @endfor
</main>
@template.common.foot()
</body>
</html>


