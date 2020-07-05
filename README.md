# obs-util

[![Build Status](https://travis-ci.org/domix/obs-util.svg?branch=master)](https://travis-ci.org/domix/obs-util)

## How to run this app as a container


```
docker run --rm -p 8080:8080 -v C:\obs\data:/data -e BASEDIR=/data domix/obs-util:0.0.8
```


```
docker run -it -v C:\obs\data:/data --rm --net=host alpine/httpie:2.2.0  --form POST :8080/v1/videos video@/data/video.yaml 
```

```
docker run -it -v C:\obs\data:/data --rm --net=host alpine/httpie:2.2.0 :8080/v1/markdownConverter video@./data/video.yaml > data/Readme.md
```

```
docker run -it -v C:\obs\data:/data --rm --net=host alpine/httpie:2.2.0 put :8080/v1/videos/goo
```

```
docker run -it -v C:\obs\data:/data --rm --net=host alpine/httpie:2.2.0 :8080/v1/videos/_active/resource/start
```