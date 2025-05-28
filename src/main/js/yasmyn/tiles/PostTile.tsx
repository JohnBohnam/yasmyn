import AsyncStorage from "@react-native-async-storage/async-storage";
import { Post, User } from "../Model"
import React, { useState } from "react";
import { View, Image, Text, StyleSheet, Button } from "react-native";


let imagePrefix = 'http://localhost:8080/uploads/';

const sendLike = async (postId: number) => {
        try {
            const authToken = await AsyncStorage.getItem('authToken');
            if (!authToken) throw new Error('Authentication token is missing');
            
            // const method = liked ? 'DELETE' : 'POST';
            const method = 'POST';
            const url = `http://localhost:8080/posts/${postId}/likes`;
            console.log('like url:', url);
            const response = await fetch(url, {
                method,
                headers: { 
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${authToken}`,
                 },
            });

            if (response.ok) {
                // setLiked(!liked);
                // setLikesCount(prev => liked ? prev - 1 : prev + 1);
                console.log("Like toggled successfully");
            } else {
                console.error("Failed to toggle like");
            }
        } catch (err) {
            console.error("Error toggling like:", err);
        }
    };

const PostTile: React.FC<{ post: Post; }> = ({ post }) => {
    // const [liked, setLiked] = useState(post.likedBy.includes(userId)); // Add `likedBy: number[]` to Post type
    const [likesCount, setLikesCount] = useState(post.likes);

    const toggleLike = async () => {
        // try {
        sendLike(post.id);
        setLikesCount(prev => prev + 1); // Optimistically update likes count
        // } catch (error) {
        //     console.error('Error toggling like:', error);
    }    
    

    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <View>
                    <Text style={styles.username}>{post.user.username}</Text>
                    <Text style={styles.date}>{new Date(post.createdAt).toLocaleDateString()}</Text>
                </View>
            </View>
            <Image
                source={{ uri: imagePrefix + post.picture.filename }}
                style={styles.postImage}
                resizeMode="cover"
            />
            <View style={styles.footer}>
                {/* <Button title={`${liked ? '💔' : '❤️'} ${likesCount}`} onPress={toggleLike} /> */}
                <Button title={`❤️ ${likesCount}`} onPress={toggleLike} />
                <Button title={`💬 ${post.comments.length}`} onPress={() => {}} />
            </View>
        </View>
    );
};


const styles = StyleSheet.create({
    container: {
        borderWidth: 1,
        borderColor: "#e0e0e0",
        borderRadius: 8,
        maxWidth: 400,
        marginVertical: 16,
        marginHorizontal: "auto",
        backgroundColor: "#fff",
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 8,
        elevation: 2,
    },
    header: {
        flexDirection: "row",
        alignItems: "center",
        padding: 16,
    },
    avatar: {
        width: 40,
        height: 40,
        borderRadius: 20,
        marginRight: 12,
    },
    username: {
        fontWeight: "600",
    },
    date: {
        fontSize: 12,
        color: "#888",
    },
    postImage: {
        // width: "100%",
        height: 300,
        width: 300,
    },
    footer: {
        flexDirection: "row",
        justifyContent: "space-between",
        paddingHorizontal: 16,
        paddingVertical: 12,
    },
});

export default PostTile;