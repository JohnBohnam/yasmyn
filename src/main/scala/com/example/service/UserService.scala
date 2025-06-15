package com.example.service

import com.example.models.DTO.UserDTO
import com.example.models.User
import com.example.repositories.{PictureRepository, PostRepository, UserRepository}

import scala.concurrent.{ExecutionContext, Future}

class UserService(postRepo: PostRepository, pictureRepository: PictureRepository)(implicit ec: ExecutionContext) {
  def toUserDTO(user: User): Future[UserDTO] = {
    for {
      postOpt <- postRepo.getLatestPost(user.id)
      imageId = postOpt.map(_.pictureId).getOrElse(0L)
      pictureOpt <- pictureRepository.findById(imageId)
      imageUrl = pictureOpt.map(_.filename).getOrElse("")
    } yield UserDTO(
      id = user.id,
      username = user.username,
      email = user.email,
      imageUrl = imageUrl
    )
  }
}
