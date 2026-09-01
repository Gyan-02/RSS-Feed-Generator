const feedTitle = document.getElementById("feed-title");
const website = document.getElementById("website");
const description = document.getElementById("description");
const author = document.getElementById("author");
const category = document.getElementById("category");

const itemTitle = document.getElementById("item-title");
const articleUrl = document.getElementById("article-url");
const itemDescription = document.getElementById("item-description");

const addButton = document.querySelector("button");
const generateButton = document.querySelector(".generate");
const preview = document.querySelector(".feed-box");
const rssOutput = document.getElementById("rss-output");

let feedItems = [];

addButton.addEventListener("click", function () {

    if (itemTitle.value === "" ||
        articleUrl.value === "" ||
        itemDescription.value === "") {

        alert("Please enter all article details.");
        return;
    }

    let item = {
        title: itemTitle.value,
        url: articleUrl.value,
        description: itemDescription.value
    };

    feedItems.push(item);

    itemTitle.value = "";
    articleUrl.value = "";
    itemDescription.value = "";

    displayFeedItems();
});

function displayFeedItems() {

    let oldArticles = preview.querySelectorAll("article");

    for (let i = 0; i < oldArticles.length; i++) {
        oldArticles[i].remove();
    }

    for (let i = 0; i < feedItems.length; i++) {

        let article = document.createElement("article");

        let title = document.createElement("h3");
        title.textContent = feedItems[i].title;

        let text = document.createElement("p");
        text.textContent = feedItems[i].description;

        let link = document.createElement("a");
        link.textContent = "Read Article";
        link.href = feedItems[i].url;
        link.target = "_blank";

        article.appendChild(title);
        article.appendChild(text);
        article.appendChild(link);

        preview.appendChild(article);
    }
}

generateButton.addEventListener("click", function () {

    if (feedTitle.value === "" ||
        website.value === "" ||
        description.value === "" ||
        author.value === "") {

        alert("Please fill in the feed details.");
        return;
    }

    if (feedItems.length === 0) {
        alert("Please add at least one feed item.");
        return;
    }

    let rss = '<?xml version="1.0" encoding="UTF-8"?>\n';
    rss += '<rss version="2.0">\n';
    rss += '    <channel>\n';
    rss += '        <title>' + feedTitle.value + '</title>\n';
    rss += '        <link>' + website.value + '</link>\n';
    rss += '        <description>' + description.value + '</description>\n';
    rss += '        <author>' + author.value + '</author>\n';
    rss += '        <category>' + category.value + '</category>\n';

    for (let i = 0; i < feedItems.length; i++) {
        rss += '        <item>\n';
        rss += '            <title>' + feedItems[i].title + '</title>\n';
        rss += '            <link>' + feedItems[i].url + '</link>\n';
        rss += '            <description>' + feedItems[i].description + '</description>\n';
        rss += '        </item>\n';
    }

    rss += '    </channel>\n';
    rss += '</rss>';

    rssOutput.textContent = rss;
});