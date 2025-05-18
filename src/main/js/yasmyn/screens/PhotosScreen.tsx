import React, {useEffect, useLayoutEffect, useState} from 'react';
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


const { height: SCREEN_HEIGHT } = Dimensions.get('window');

async function fetchEveryonesImages(setOthersImages: React.Dispatch<React.SetStateAction<string[]>>, setLoading: React.Dispatch<React.SetStateAction<boolean>>) {
    try {
        const authToken = await AsyncStorage.getItem('authToken');

        if (!authToken) {
            throw new Error('Authentication token is missing');
        }

        const response = await fetch('http://localhost:8080/pictures', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        const imageUrls = data.map((image: any) => image.filename);
        setOthersImages(imageUrls);
    } catch (error) {
        console.error('Fetch failed:', error);
        Alert.alert('Error fetching images', String(error));
    } finally {
        setLoading(false);
    }
}

const PhotosScreen = ({ route } : any) => {
    const [othersImages, setOthersImages] = useState<string[]>([]);
    const [loading, setLoading] = useState(true);
    const { isVoting } = route.params;

    useEffect(() => {
        fetchEveryonesImages(setOthersImages, setLoading);
    }, []);

    console.log(isVoting)

    // Render each photo
    let imagePrefix = 'http://localhost:8080/uploads/';
    const renderItem = ({ item }: { item: string }) => (
        <View style={styles.imageContainer}>
            <Image source={{ uri: imagePrefix + item }} style={styles.image} />
        </View>
    );

    if (loading) {
        return <ActivityIndicator size="large" color="#0000ff" />;
    }

    if (Platform.OS === 'web') {
        return (
            <ScrollView style={styles.webScroller}>
                {othersImages.map((item, index) => (
                    <View key={index.toString()} style={styles.imageContainer}>
                        <Image source={{ uri: imagePrefix + item }} style={styles.image} />
                    </View>
                ))}
            </ScrollView>
        );
    }

    return (
        <FlatList
            data={othersImages}
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