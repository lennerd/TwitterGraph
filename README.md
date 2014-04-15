TwitterGraph
============

## Idea

TwitterGraph is a Twitter data visualiziation built with [Processing](http://www.processing.org/). It simple approach is to visualize geolocalized
data from the TwitterAPI. The result is a imaginary map with points, circles and lines.

![TwitterGraph](https://incom.org/action/open-image/118434/big/118434.png)

For more information see:<br>
https://vimeo.com/59059352<br>
https://incom.org/projekt/2926

## How to use it?

Before we can start download the [source](https://github.com/lennerd/TwitterGraph/archive/master.zip) and get your own TwitterAPI OAuth consumer key, consumer secret, access token and access token secret from [here](https://apps.twitter.com/).

Next open your Console and go to the extracted source. Execute the JAR file with your new OAuth API credentials:

    $ java -jar build/TwitterGraph.jar -k <consumer-key> -s <consumer-secret> -t <access-token> -S <access-token-secret>
    
Have fun watching and exploring your TwitterGraph.

## Contribute

Feel free to contribute in any kind. Unfortunately there is no guide showing at least the main features. But if you feel confident enough, don't hasitate to take a look in the code or make your pull request.

## Future plans

There is no documentation or real "How to use it?" guide yet. I plan to build a Github page for it and decouple some components I find to be useful when working with the TwitterAPI and Processing. I also would like to build some more features like searching for hashtags.
