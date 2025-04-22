import React, { useState, useEffect } from 'react';
import {View, Text, StyleSheet, Alert, Button, Image, Platform} from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import AsyncStorage from "@react-native-async-storage/async-storage";

async function uploadImage(imageUri: string) {
    try {
        const authToken = await AsyncStorage.getItem('authToken');

        if (!authToken) {
            throw new Error('Authentication token is missing');
        }

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

        const response = await fetch('http://localhost:8080/pictures', {
            method: 'POST',
            body: formData,
            headers: {
                'Authorization': `Bearer ${authToken}`, // only this header is needed
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.text();
        console.log('Upload successful:', data);
    } catch (error) {
        console.error('Upload failed:', error);
    }
}

export default function MainScreen() {
    const [topic, setTopic] = useState('');
    const [imageUri, setImageUri] = useState<string | null>(null); // State to store the image URI

    useEffect(() => {
        const fetchTopic = async () => {
            try {
                const response = await fetch('http://localhost:8080/topic/today');
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data = await response.json();
                setTopic(data.topic); // Assuming the response has a `topic` field
            } catch (err: any) {
                Alert.alert('Error', err.message);
            }
        };

        fetchTopic();
    }, []);

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
            let imageUri = result.assets[0].uri;
            setImageUri(imageUri);

            await uploadImage(imageUri);

            // probably change this to: try to upload, then set imageUri if successful

        }
    };

    return (
        <View style={styles.container}>
            <Text style={styles.text}>Today's Topic:</Text>
            <Text style={styles.topic}>{topic || 'Loading...'}</Text>

            <Button title="Select an Image" onPress={handleSelectImage} />

            {/* Display the selected image */}
            {imageUri && (
                <Image source={{ uri: imageUri }} style={styles.image} />
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
    text: { fontSize: 18, fontWeight: 'bold' },
    topic: { fontSize: 16, marginTop: 10 },
    image: {
        width: 200,
        height: 200,
        marginTop: 20,
        borderRadius: 10,
        borderWidth: 2,
        borderColor: '#ccc',
    },
});