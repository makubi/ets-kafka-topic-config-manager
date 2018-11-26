#!/usr/bin/env bash

set -e

SCRIPT_DIR="$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
EXAMPLE_MODULE_DIR="$SCRIPT_DIR"

MAIN_CLASS="de.kaufhof.ets.kafkatopicconfigmanager.Main"

echo "===== Building configuration application"
mvn -B -f "$PROJECT_ROOT/pom.xml" clean package

MANAGER_TMP_DIR="$(mktemp -d -p "$EXAMPLE_MODULE_DIR/target")"
CONFIGS_TMP_DIR="$(mktemp -d -p "$EXAMPLE_MODULE_DIR/target")"
TMP_LOG_FILE="$(mktemp -p "$EXAMPLE_MODULE_DIR/target")"

docker-compose -f "$EXAMPLE_MODULE_DIR/docker-compose.yaml" -p ets-kafka-topic-config-manager-example down
docker-compose -f "$EXAMPLE_MODULE_DIR/docker-compose.yaml" -p ets-kafka-topic-config-manager-example up -d
trap "docker-compose -f "$EXAMPLE_MODULE_DIR/docker-compose.yaml" -p ets-kafka-topic-config-manager-example stop" EXIT

echo "===== Extracting manager"
tar -xf "$PROJECT_ROOT"/ets-kafka-topic-config-manager-core/target/ets-kafka-topic-config-manager-core-*-ets-assembly-descriptors-application.tar.gz -C "$MANAGER_TMP_DIR"
tar -xf "$EXAMPLE_MODULE_DIR"/target/ets-kafka-topic-config-manager-example-*-ets-assembly-descriptors-application.tar.gz -C "$CONFIGS_TMP_DIR"

function check_topic_change_detection {
    local TOPIC="$1"

    java -cp "$MANAGER_TMP_DIR/*:$CONFIGS_TMP_DIR/*" "$MAIN_CLASS" | tee "$TMP_LOG_FILE"

    if ! grep -q "TopicIsMissingForAllEnvironmentsOnKafka(XmlTopic(Topic($TOPIC),None,XmlEnvironments(ArrayBuffer(XmlEnvironment(test,XmlProperties(XmlConfigs(XmlCleanupPolicy(None,Some(XmlDelete(Some(604800000)))),None,Some(1),None,Some(true)),1,16))))))" "$TMP_LOG_FILE"; then
        return 1
    fi
}

function check_topic_application {
    local TOPIC="$1"

    java -DautoApply=true -cp "$MANAGER_TMP_DIR/*:$CONFIGS_TMP_DIR/*" "$MAIN_CLASS" | tee "$TMP_LOG_FILE"
    echo $?

    if ! docker-compose -f "$EXAMPLE_MODULE_DIR/docker-compose.yaml" -p ets-kafka-topic-config-manager-example run --rm kafka kafka-topics --zookeeper zookeeper:2181 --list | grep -q "$TOPIC"; then
        return 1
    fi
}


echo "===== Checking that changes are detected"
if ! check_topic_change_detection "MyEvent-v1"; then
    echo "===== Did not find change for topic MyEvent-v1"
    exit 1
fi

echo "===== Checking that changes are applied"
if ! check_topic_application "MyEvent-v1"; then
    echo "===== Did not find topic MyEvent-v1"
    exit 1
fi

echo "===== Checking that no changes are detected"
if check_topic_change_detection "MyEvent-v1"; then
    echo "===== Found change for topic MyEvent-v1"
    exit 1
fi
