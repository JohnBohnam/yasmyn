import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    StyleSheet,
    Alert,
    Button,
    Image,
    Platform,
    ScrollView,
    SafeAreaView,
    TouchableOpacity, Dimensions
} from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import AsyncStorage from "@react-native-async-storage/async-storage";
import {useNavigation} from "@react-navigation/native";

const { width } = Dimensions.get('window');
const CIRCLE = 80;

async function uploadImage(imageUri: string, setImageUri: (uri: string | null) => void) {
    try {
        const authToken = await AsyncStorage.getItem('authToken');
        if (!authToken) throw new Error('Authentication token is missing');

        const formData = new FormData();

        if (Platform.OS === 'web') {
            const response = await fetch(imageUri);
            const blob = await response.blob();
            formData.append('image', blob, 'upload.jpg');
        } else {
            formData.append('image', {
                uri: imageUri,
                type: 'image/jpeg',
                name: 'upload.jpg',
            } as any);
        }

        const response = await fetch('http://localhost:8080/posts', {
            method: 'POST',
            body: formData,
            headers: {
                'Authorization': `Bearer ${authToken}`,
            },
        });

        if (!response.ok) {
            const errText = await response.text();
            throw new Error(`Upload failed: ${response.status} - ${errText}`);
        }

        const data = await response.json();

        setImageUri(imageUri); // Set image only if upload was successful
        Alert.alert('Success', 'Image uploaded successfully!');

    } catch (error: any) {
        console.error('Upload failed:', error.message);
        Alert.alert('Error', error.message);
        setImageUri(null); // Clear image if upload failed
    }
}

function formatTime(seconds: number): string {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secondsLeft = seconds % 60

    const pad = (n: number) => n.toString().padStart(2, '0')
    return `${pad(hours)}:${pad(minutes)}:${pad(secondsLeft)}`
}


// @ts-ignore
export default function MainScreen({ navigation }) {
    const [topic, setTopic] = useState('');
    const [imageUri, setImageUri] = useState<string | null>(null);
    const [topicExpiration, setTopicExpiration] = useState<Date | null>(null);
    const [timeLeft, setTimeLeft] = useState<number>(0);


    const fetchTopic = async () => {
        try {
            const response = await fetch('http://localhost:8080/topic/today');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            setTopicExpiration(new Date(data.expiresAt));
            setTopic(data.topic);
        } catch (err: any) {
            Alert.alert('Error', err.message);
        }
    };

    useEffect(() => {
        fetchTopic();
        const interval = setInterval(fetchTopic, 2*1000);
        return () => clearInterval(interval);
    }, []);


    useEffect(() => {
        if (!topicExpiration) return;

        const now = new Date();
        const msUntilExpiration = topicExpiration.getTime() - now.getTime();

        if (msUntilExpiration <= 0) return;

        const timeout = setTimeout(() => {
            // Re-fetch topic after it expires
            fetchTopic();
        }, msUntilExpiration + 1000); // slight buffer

        return () => clearTimeout(timeout);
    }, [topicExpiration]);
    

    useEffect(() => {
    if (!topicExpiration) return;

    const interval = setInterval(() => {
        const now = new Date();
        const diff = Math.max(0, Math.floor((topicExpiration.getTime() - now.getTime()) / 1000));
        setTimeLeft(diff);
    }, 1000);

    return () => clearInterval(interval);
    }, [topicExpiration]);


    // Request camera roll permissions (important for Expo)
    useEffect(() => {
        (async () => {
            const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
            if (status !== 'granted') {
                Alert.alert('Permission required', 'Permission to access media is required!');
            }
        })();
    }, []);

    // Function to handle image selection
    const handleSelectImage = async () => {
    let result = await ImagePicker.launchImageLibraryAsync({
        mediaTypes: ImagePicker.MediaTypeOptions.Images,
        allowsEditing: true,
        aspect: [4, 3],
        quality: 1,
    });

    if (!result.canceled) {
        const uri = result.assets[0].uri;
        await uploadImage(uri, setImageUri); // Upload and only then show
    }
};


    return (
        <SafeAreaView style={styles.safe}>
            {/* HEADER */}
            <View style={styles.header}>
                <TouchableOpacity 
                    style={styles.circleButton}
                    onPress={() => navigation.navigate("MyProfile")}
                > 
                    <Text style={styles.circleText}>My{"\n"}Profile</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.circleButton}>
                    <Text style={styles.circleText}>Options</Text>
                </TouchableOpacity>
            </View>

            {/* SCROLLABLE BODY */}
            <ScrollView contentContainerStyle={styles.body}>
            <Text style={styles.bigTitle}>PICTURE OF THE DAY:</Text>
            <Text style={styles.topic}>{topic || "Loading..."}</Text>
            {topicExpiration && (
                <Text style={styles.expiration}>
                Topic expires at: {topicExpiration.toLocaleTimeString()}
                </Text>
            )}

            <TouchableOpacity onPress={handleSelectImage} style={styles.imagePicker}>
                {imageUri
                ? <Image source={{ uri: imageUri }} style={styles.mainImage} />
                : <View style={styles.cameraPlaceholder}><Text>Choose Image</Text></View>}
            </TouchableOpacity>

            <Text style={styles.timer}>TIME LEFT: {formatTime(timeLeft)}</Text>
            </ScrollView>


            {/* FOOTER */}
            <View style={styles.footer}>
                <TouchableOpacity style={styles.circleButton}
                                  onPress={() => navigation.navigate("Photos")}>
                    <Image source={require("../assets/photos.png")} style={styles.imageButton}/>
                </TouchableOpacity>
                <TouchableOpacity style={styles.circleButton} >
                    <Image source={require("../assets/friends.png")} style={styles.imageButton}/>
                </TouchableOpacity>
                <TouchableOpacity style={styles.circleButton}>
                    <Image source={require("../assets/search.png")} style={styles.imageButton}/>
                </TouchableOpacity>
            </View>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    safe: { flex: 1, backgroundColor: "#fff" },
    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        padding: 16
    },
    circleButton: {
        width: CIRCLE,
        height: CIRCLE,
        alignItems: "center",
        justifyContent: "center"
    },
    circleText: {
        fontSize: 12,
        textAlign: "center"
    },
    body: {
        alignItems: "center",
        paddingVertical: 20,
        paddingHorizontal: 16,
        flex: 1
    },
    bigTitle: {
        fontSize: 18,
        fontWeight: "bold",
        marginBottom: 4
    },
    topic: {
        fontSize: 16,
        marginBottom: 20
    },
    imagePicker: {
        width: width * 0.5,
        height: width * 0.5,
        maxWidth: 500,
        maxHeight: 500,
        borderRadius: (width * 0.5) / 2,
        borderWidth: 2,
        borderColor: "#000",
        overflow: "hidden",
        alignItems: "center",
        justifyContent: "center",
        marginBottom: 12
    },
    cameraPlaceholder: {
        alignItems: "center",
        justifyContent: "center"
    },
    mainImage: {
        width: "100%",
        height: "100%"
    },
    timer: {
        fontSize: 14,
        marginTop: 8
    },
    footer: {
        flexDirection: "row",
        justifyContent: "space-around",
        paddingVertical: 16,
        borderTopWidth: 1,
        borderColor: "#ccc"
    },
    imageButton: {
        width: CIRCLE,
        height: CIRCLE
    },
    expiration: {
        fontSize: 14,
        color: '#888',
        marginTop: 4,
        marginBottom: 10,
        textAlign: 'center',
    },
});