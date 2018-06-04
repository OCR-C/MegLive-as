#!/usr/bin/env bash

set -x
set -e

rm -rf ../MegLive-eclipse/LivenessLib/bin
rm -rf ../MegLive-eclipse/LivenessLib/gen
rm -rf ../MegLive-eclipse/LivenessProject/bin
rm -rf ../MegLive-eclipse/LivenessProject/gen
rm -rf ../MegLive-eclipse/LivenessLib/libs/*
cp -r ./LivenessLib/libs/* ../MegLive-eclipse/LivenessLib/libs/
rm -rf ../MegLive-eclipse/LivenessLib/res/*
cp -r ./LivenessLib/src/main/res/* ../MegLive-eclipse/LivenessLib/res/
rm -rf ../MegLive-eclipse/LivenessLib/src/*
cp -r ./LivenessLib/src/main/java/* ../MegLive-eclipse/LivenessLib/src/
rm -rf ../MegLive-eclipse/LivenessProject/res/*
cp -r ./app/src/main/res/* ../MegLive-eclipse/LivenessProject/res/
rm -rf ../MegLive-eclipse/LivenessProject/src/*
cp -r ./app/src/main/java/* ../MegLive-eclipse/LivenessProject/src/