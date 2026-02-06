@param gg.jte.Content content
@param String title

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
</head>
<body>
<nav>
    <ul>
        <li><a href="/">Home</a></li>
        <li><a href="/about">About</a></li>
        <li><a href="/addcity">Add City</a></li>
    </ul>
</nav>

<main>
    ${content}
</main>
@template.common.foot()
</body>
</html>