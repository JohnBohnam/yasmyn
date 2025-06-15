import AsyncStorage from "@react-native-async-storage/async-storage";
import {Post, User} from "../Model";
import React, {useEffect, useState} from "react";
import {View, Image, Text, StyleSheet, Button, Alert} from "react-native";
import {API_BASE_URL} from "../constants";

let imagePrefix = API_BASE_URL + '/uploads/';
const API_URL = API_BASE_URL;

const sendLike = async (postId: number, liked: boolean): Promise<boolean> => {
    try {
        const authToken = await AsyncStorage.getItem('authToken');
        if (!authToken) throw new Error('Authentication token is missing');

        const method = liked ? 'DELETE' : 'POST';
        const url = `${API_BASE_URL}/posts/${postId}/likes`;
        console.log(`Sending ${method} request to ${url}`);
        const response = await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`,
            },
        });

        if (response.ok) {
            return true;
        } else {
            console.error("Failed to toggle like");
            return false;
        }
    } catch (err) {
        console.error("Error toggling like:", err);
        return false;
    }
};


const observeUserById = async (id: number, setObserveDisabled: React.Dispatch<React.SetStateAction<boolean>>) => {
    try {
        const authToken = await AsyncStorage.getItem("authToken");
        if (!authToken) throw new Error("Authentication token is missing");

        const response = await fetch(`${API_URL}/me/observe`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${authToken}`,
            },
            body: JSON.stringify({observedUserId: id}),
        });

        if (response.status === 409) {
            Alert.alert("Already observing this user.");
        } else if (!response.ok) {
            throw new Error("Failed to observe user");
        }
        setObserveDisabled(true);
    } catch (error) {
        Alert.alert("Error", "Could not add observed user");
    }
};

const fetchObserved = async (id: number, setObserveDisabled: (disabled: boolean) => void) => {
    const authToken = await AsyncStorage.getItem("authToken");
    if (!authToken) throw new Error("Authentication token is missing");

    try {
        const [obsRes] = await Promise.all([
            fetch(`${API_URL}/me/observed`, {
                headers: {Authorization: `Bearer ${authToken}`},
            }),
        ]);

        if (!obsRes.ok) throw new Error("Fetch failed");


        const observedUsers: User[] = await obsRes.json();

        if (observedUsers.some((user) => user.id === id)) {
            setObserveDisabled(true);
        } else {
            setObserveDisabled(false);
        }

    } catch (error) {
        Alert.alert("Error", "Could not load observe data");
    }
};

const PostTile: React.FC<{ post: Post; disableLike?: boolean, place?: number }> = ({ post, disableLike = false, place = -1 }) => {
    const [liked, setLiked] = useState(post.isLiked); // assume this is a boolean field now
    const [likesCount, setLikesCount] = useState(post.likes);
    const [observeDisabled, setObserveDisabled] = useState(false);

    console.log("topic: ", post.topic.content, "liked: ", post.isLiked);


    useEffect(() => {
        fetchObserved(post.user.id, setObserveDisabled);
    }, []);

    useEffect(() => {
        setLiked(post.isLiked);
    }, [post.id, post.isLiked]);

    const toggleLike = async () => {
        const success = await sendLike(post.id, liked);
        if (success) {
            setLiked(!liked);
            setLikesCount(prev => liked ? prev - 1 : prev + 1);
        }
    };

    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 10 }}>
                    <Image
                    source={{ uri: imagePrefix + post.user.imageUrl }}
                    style={styles.profileImage}
                    />
                    <View>
                    <Text style={styles.username}>{post.user.username}</Text>
                    <Text style={styles.date}>{new Date(post.createdAt).toLocaleDateString()}</Text>
                    <Text style={styles.topic}>Topic: {post.topic.content}</Text>
                    </View>
                </View>
            </View>
            <Image
                source={{ uri: imagePrefix + post.picture.filename }}
                style={styles.postImage}
                resizeMode="cover"
            />
            <View style={styles.footer}>
            {place == 1 ? <Text style={{ fontSize: 14, fontWeight: 'bold', marginVertical: 10 }}>CURRENT WINNER, LIKES: {likesCount}</Text> :
                place != -1 && <Text style={{ fontSize: 14, fontWeight: 'bold', marginVertical: 10 }}>CURRENT PLACE: {place}, LIKES: {likesCount}</Text>}

                 {!disableLike ? <Button
                    title={`${liked ? '💔' : '❤️'} ${likesCount}`}
                    onPress={toggleLike}
                /> : place == -1 && <Text>LIKES: {likesCount}</Text>
            }
                <Button disabled={observeDisabled}
                    title={`🙋‍♂️ `} onPress={() => observeUserById(post.user.id, setObserveDisabled)} />
            </View>
        </View>
    );
};

// const styles = StyleSheet.create({
//     container: {
//         borderWidth: 1,
//         borderColor: "#e0e0e0",
//         borderRadius: 8,
//         maxWidth: 400,
//         marginVertical: 16,
//         marginHorizontal: "auto",
//         backgroundColor: "#fff",
//         shadowColor: "#000",
//         shadowOffset: { width: 0, height: 2 },
//         shadowOpacity: 0.05,
//         shadowRadius: 8,
//         elevation: 2,
//     },
//     header: {
//         flexDirection: "row",
//         alignItems: "flex-start",
//         padding: 16,
//     },
//     username: {
//         fontWeight: "600",
//     },
//     date: {
//         fontSize: 12,
//         color: "#888",
//     },
//     topic: {
//         fontSize: 14,
//         color: "#555",
//         marginTop: 4,
//         fontStyle: 'italic'
//     },
//     postImage: {
//         height: 300,
//         width: 300,
//     },
//     footer: {
//         flexDirection: "row",
//         justifyContent: "space-between",
//         paddingHorizontal: 16,
//         paddingVertical: 12,
//     },
// });

// export default PostTile;

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
    topic: {
        fontSize: 14,
        color: "#555",
        marginTop: 4,
        fontStyle: 'italic'
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
        profileImage: {
    width: 40,
    height: 40,
    borderRadius: 20,
    marginRight: 8,
    },
    footer: {
        flexDirection: "row",
        justifyContent: "space-between",
        paddingHorizontal: 16,
        paddingVertical: 12,
    },
});

export default PostTile;