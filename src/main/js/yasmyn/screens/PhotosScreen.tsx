import React, { useEffect, useLayoutEffect, useState } from 'react';
import {
    View,
    FlatList,
    Image,
    StyleSheet,
    Text,
    ActivityIndicator,
    Alert,
    Dimensions,
    Platform,
    ScrollView
} from 'react-native';
import AsyncStorage from "@react-native-async-storage/async-storage";
import PostTile from '../tiles/PostTile';
import { Post } from '../Model';
import {API_BASE_URL} from "../constants";


const { height: SCREEN_HEIGHT } = Dimensions.get('window');

function parsePostDates(post: any) {
  return {
    ...post,
    createdAt: new Date(post.createdAt),
  };
}

async function fetchEveryonesImages(setOthersPosts: React.Dispatch<React.SetStateAction<Post[]>>, setLoading: React.Dispatch<React.SetStateAction<boolean>>) {
    try {
        const authToken = await AsyncStorage.getItem('authToken');

        if (!authToken) {
            throw new Error('Authentication token is missing');
        }
        //const topicId = await fetchTodayTopic(); mozna dac topic id do linku jakbysmy chcieli zdjecia z tylko dzisiaj
        const response = await fetch(`${API_BASE_URL}/posts`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        const posts = data.map(parsePostDates) as Post[];
        console.log('Fetched posts:', posts);
        setOthersPosts(posts);
    } catch (error) {
        console.error('Fetch failed:', error);
        Alert.alert('Error fetching images', String(error));
    } finally {
        setLoading(false);
    }
}


async function fetchTodayTopicId(): Promise<number> {
    try {
        const response = await fetch(`${API_BASE_URL}/topic/today`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        return data.topicId;
    } catch (err: any) {
        Alert.alert('Error', err.message);
    }
    return -1;
};

// @ts-ignore
const PhotosScreen = ({ navigation }) => {
    const [othersPosts, setOthersPosts] = useState<Post[]>([]);
    const [loading, setLoading] = useState(true);
    const [topicId, setTopicId] = useState<number>(-1);

    useEffect(() => {
        fetchEveryonesImages(setOthersPosts, setLoading);
        fetchTodayTopicId().then(setTopicId);
    }, []);

    const authToken = AsyncStorage.getItem('authToken');
    // if (!authToken)

    const renderItem = ({ item, index }: { item: Post, index: number }) => (
        <View key={index.toString()}>   
            <PostTile post={item} disableLike={item.topic.id != topicId} />
        </View>
    );


    if (loading) {
        return <ActivityIndicator size="large" color="#0000ff" />;
    }

    if (Platform.OS === 'web') {
        return (
            <ScrollView style={styles.webScroller}>
                {othersPosts.map((item, index) => (
                    renderItem({ item, index })
                ))}
            </ScrollView>
        );
    }

    return (
        <View>
            <Text>Hello</Text>
            <FlatList
                data={othersPosts}
                keyExtractor={(item, index) => index.toString()}
                renderItem={renderItem}
                contentContainerStyle={styles.listContainer}
                showsVerticalScrollIndicator={false}
                pagingEnabled={true}
                snapToInterval={SCREEN_HEIGHT}
                snapToAlignment="start"
                decelerationRate="fast"
                getItemLayout={(data, index) => (
                    { length: SCREEN_HEIGHT, offset: SCREEN_HEIGHT * index, index }
                )}
            />  
        </View>
        
    );
};

const styles = StyleSheet.create({
    listContainer: {
        paddingTop: 0,
    },
    imageContainer: {
        height: SCREEN_HEIGHT,
    },
    webScroller: {
        height: 100,
        overflow: 'scroll',
    },
    image: {
        width: '100%',
        height: '100%',
        resizeMode: 'cover',
    },
    imageOverlay: {
        height: SCREEN_HEIGHT,
        ...StyleSheet.absoluteFillObject,
        backgroundColor: 'rgba(122, 33, 0, 0.3)', // Dark filter
        borderRadius: 10,
    },
});

export default PhotosScreen;