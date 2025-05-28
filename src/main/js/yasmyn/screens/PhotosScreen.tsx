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

        const response = await fetch('http://localhost:8080/posts', {
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

const PhotosScreen = ({ route }: any) => {
    const [othersPosts, setOthersPosts] = useState<Post[]>([]);
    const [loading, setLoading] = useState(true);
    const { isVoting } = route.params;

    useEffect(() => {
        fetchEveryonesImages(setOthersPosts, setLoading);
    }, []);

    console.log(isVoting)

    // Render each photo
    // const renderItem = ({ item }: { item: string }) => (
    //     <View style={styles.imageContainer}>
    //         <Image source={{ uri: imagePrefix + item }} style={styles.image} />
    //     </View>
    // );

    const authToken = AsyncStorage.getItem('authToken');
    // if (!authToken)

    const renderItem = ({ item }: { item: Post }) => (
        <PostTile post={item} />
    );



    if (loading) {
        return <ActivityIndicator size="large" color="#0000ff" />;
    }

    if (Platform.OS === 'web') {
        return (
            <ScrollView style={styles.webScroller}>
                {othersPosts.map((item, index) => (
                    // <View key={index.toString()} style={styles.imageContainer}>
                    //     <Image source={{ uri: imagePrefix + item }} style={styles.image} />
                    // </View>
                    console.log(item),
                    renderItem({ item: item })
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